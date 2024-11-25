// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.java.decompiler.modules.decompiler.vars;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.DirectGraph;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.FlattenStatementsHelper;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.SSAConstructorSparseEx;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.DotExporter;
import org.jetbrains.java.decompiler.util.FastSparseSetFactory.FastSparseSet;

import java.util.*;
import java.util.Map.Entry;

public class VarVersionsProcessor {
  private final StructMethod method;
  private Map<Integer, VarVersion> mapOriginalVarIndices = Collections.emptyMap();
  private final VarTypeProcessor typeProcessor;

  public VarVersionsProcessor(StructMethod mt, MethodDescriptor md) {
    method = mt;
    typeProcessor = new VarTypeProcessor(mt, md);
  }

  public void setVarVersions(RootStatement root, VarVersionsProcessor previousVersionsProcessor) {
    SSAConstructorSparseEx ssa = new SSAConstructorSparseEx();
    ssa.splitVariables(root, method);

    FlattenStatementsHelper flattenHelper = new FlattenStatementsHelper();
    DirectGraph graph = flattenHelper.buildDirectGraph(root);

    DotExporter.toDotFile(graph, method, "setVarVersions");

    mergePhiVersions(ssa, graph);

    typeProcessor.calculateVarTypes(root, graph);

    //simpleMerge(typeProcessor, graph, method);

    // FIXME: advanced merging

    eliminateNonJavaTypes(typeProcessor);

    setNewVarIndices(typeProcessor, graph, previousVersionsProcessor);
  }

  private static void mergePhiVersions(SSAConstructorSparseEx ssa, DirectGraph graph) {
    // collect phi versions
    List<Set<VarVersion>> lst = new ArrayList<>();
    for (Entry<VarVersion, FastSparseSet<Integer>> ent : ssa.getPhi().entrySet()) {
      Set<VarVersion> set = new HashSet<>();
      set.add(ent.getKey());
      for (Integer version : ent.getValue()) {
        set.add(new VarVersion(ent.getKey().var, version.intValue()));
      }

      for (int i = lst.size() - 1; i >= 0; i--) {
        Set<VarVersion> tset = lst.get(i);
        Set<VarVersion> intersection = new HashSet<>(set);
        intersection.retainAll(tset);

        if (!intersection.isEmpty()) {
          set.addAll(tset);
          lst.remove(i);
        }
      }

      lst.add(set);
    }

    Map<VarVersion, Integer> phiVersions = new HashMap<>();
    for (Set<VarVersion> set : lst) {
      int min = Integer.MAX_VALUE;
      for (VarVersion paar : set) {
        if (paar.version < min) {
          min = paar.version;
        }
      }

      for (VarVersion paar : set) {
        phiVersions.put(new VarVersion(paar.var, paar.version), min);
      }
    }

    updateVersions(graph, phiVersions);
  }

  private static void updateVersions(DirectGraph graph, final Map<VarVersion, Integer> versions) {
    graph.iterateExprents(exprent -> {
      List<Exprent> lst = exprent.getAllExprents(true);
      lst.add(exprent);

      for (Exprent expr : lst) {
        if (expr.type == Exprent.EXPRENT_VAR) {
          VarExprent var = (VarExprent)expr;
          Integer version = versions.get(new VarVersion(var));
          if (version != null) {
            var.setVersion(version);
          }
        }
      }

      return 0;
    });
  }

  private static void eliminateNonJavaTypes(VarTypeProcessor typeProcessor) {
    Map<VarVersion, VarType> mapExprentMaxTypes = typeProcessor.getMaxExprentTypes();
    Map<VarVersion, VarType> mapExprentMinTypes = typeProcessor.getMinExprentTypes();

    for (VarVersion paar : new ArrayList<>(mapExprentMinTypes.keySet())) {
      VarType type = mapExprentMinTypes.get(paar);
      VarType maxType = mapExprentMaxTypes.get(paar);

      if (type.getType() == CodeConstants.TYPE_BYTECHAR || type.getType() == CodeConstants.TYPE_SHORTCHAR) {
        if (maxType != null && maxType.getType() == CodeConstants.TYPE_CHAR) {
          type = VarType.VARTYPE_CHAR;
        }
        else {
          type = type.getType() == CodeConstants.TYPE_BYTECHAR ? VarType.VARTYPE_BYTE : VarType.VARTYPE_SHORT;
        }
        mapExprentMinTypes.put(paar, type);
        //} else if(type.type == CodeConstants.TYPE_CHAR && (maxType == null || maxType.type == CodeConstants.TYPE_INT)) { // when possible, lift char to int
        //	mapExprentMinTypes.put(paar, VarType.VARTYPE_INT);
      }
      else if (type.getType() == CodeConstants.TYPE_NULL) {
        mapExprentMinTypes.put(paar, VarType.VARTYPE_OBJECT);
      }
    }
  }

  private static void simpleMerge(VarTypeProcessor typeProcessor, DirectGraph graph, StructMethod mt) {
    Map<VarVersion, VarType> mapExprentMaxTypes = typeProcessor.getMaxExprentTypes();
    Map<VarVersion, VarType> mapExprentMinTypes = typeProcessor.getMinExprentTypes();

    Map<Integer, Set<Integer>> mapVarVersions = new HashMap<>();

    for (VarVersion pair : mapExprentMinTypes.keySet()) {
      if (pair.version >= 0) {  // don't merge constants
        mapVarVersions.computeIfAbsent(pair.var, k -> new HashSet<>()).add(pair.version);
      }
    }

    boolean is_method_static = mt.hasModifier(CodeConstants.ACC_STATIC);

    Map<VarVersion, Integer> mapMergedVersions = new HashMap<>();

    for (Entry<Integer, Set<Integer>> ent : mapVarVersions.entrySet()) {

      if (ent.getValue().size() > 1) {
        List<Integer> lstVersions = new ArrayList<>(ent.getValue());
        Collections.sort(lstVersions);

        for (int i = 0; i < lstVersions.size(); i++) {
          VarVersion firstPair = new VarVersion(ent.getKey(), lstVersions.get(i));
          VarType firstType = mapExprentMinTypes.get(firstPair);

          if (firstPair.var == 0 && firstPair.version == 1 && !is_method_static) {
            continue; // don't merge 'this' variable
          }

          for (int j = i + 1; j < lstVersions.size(); j++) {
            VarVersion secondPair = new VarVersion(ent.getKey(), lstVersions.get(j));
            VarType secondType = mapExprentMinTypes.get(secondPair);

            if (firstType.equals(secondType) ||
                firstType.equals(VarType.VARTYPE_NULL) && secondType.getType() == CodeConstants.TYPE_OBJECT ||
                secondType.equals(VarType.VARTYPE_NULL) && firstType.getType() == CodeConstants.TYPE_OBJECT ||
                firstType.getTypeFamily() == CodeConstants.TYPE_FAMILY_INTEGER && secondType.getTypeFamily() == CodeConstants.TYPE_FAMILY_INTEGER) {
              VarType firstMaxType = mapExprentMaxTypes.get(firstPair);
              VarType secondMaxType = mapExprentMaxTypes.get(secondPair);
              VarType type = firstMaxType == null ? secondMaxType :
                             secondMaxType == null ? firstMaxType :
                             VarType.getCommonMinType(firstMaxType, secondMaxType);

              if (firstType.getTypeFamily() == CodeConstants.TYPE_FAMILY_INTEGER && secondType.getTypeFamily() == CodeConstants.TYPE_FAMILY_INTEGER) {
                type = switch (secondType.getType()) {
                  case CodeConstants.TYPE_INT -> VarType.VARTYPE_INT;
                  case CodeConstants.TYPE_SHORT -> firstType.getType() == CodeConstants.TYPE_INT ? null : VarType.VARTYPE_SHORT;
                  case CodeConstants.TYPE_CHAR -> switch (firstType.getType()) {
                    case CodeConstants.TYPE_INT, CodeConstants.TYPE_SHORT -> null;
                    default -> VarType.VARTYPE_CHAR;
                  };
                  case CodeConstants.TYPE_SHORTCHAR -> switch (firstType.getType()) {
                    case CodeConstants.TYPE_INT, CodeConstants.TYPE_SHORT, CodeConstants.TYPE_CHAR -> null;
                    default -> VarType.VARTYPE_SHORTCHAR;
                  };
                  case CodeConstants.TYPE_BYTECHAR -> switch (firstType.getType()) {
                    case CodeConstants.TYPE_INT, CodeConstants.TYPE_SHORT, CodeConstants.TYPE_CHAR, CodeConstants.TYPE_SHORTCHAR -> null;
                    default -> VarType.VARTYPE_BYTECHAR;
                  };
                  case CodeConstants.TYPE_BYTE -> switch (firstType.getType()) {
                    case CodeConstants.TYPE_INT, CodeConstants.TYPE_SHORT, CodeConstants.TYPE_CHAR, CodeConstants.TYPE_SHORTCHAR, CodeConstants.TYPE_BYTECHAR ->
                      null;
                    default -> VarType.VARTYPE_BYTE;
                  };
                  default -> type;
                };
                if (type == null) {
                  continue;
                }
                firstType = type;
                mapExprentMinTypes.put(firstPair, type);
              }

              mapExprentMaxTypes.put(firstPair, type);
              mapMergedVersions.put(secondPair, firstPair.version);
              mapExprentMaxTypes.remove(secondPair);
              mapExprentMinTypes.remove(secondPair);

              if (firstType.equals(VarType.VARTYPE_NULL)) {
                mapExprentMinTypes.put(firstPair, secondType);
                firstType = secondType;
              }

              typeProcessor.getFinalVariables().put(firstPair, VarProcessor.VAR_NON_FINAL);

              lstVersions.remove(j);
              //noinspection AssignmentToForLoopParameter
              j--;
            }
          }
        }
      }
    }

    if (!mapMergedVersions.isEmpty()) {
      updateVersions(graph, mapMergedVersions);
    }
  }

  private void setNewVarIndices(VarTypeProcessor typeProcessor, DirectGraph graph, VarVersionsProcessor previousVersionsProcessor) {
    final Map<VarVersion, VarType> mapExprentMaxTypes = typeProcessor.getMaxExprentTypes();
    Map<VarVersion, VarType> mapExprentMinTypes = typeProcessor.getMinExprentTypes();
    Map<VarVersion, Integer> mapFinalVars = typeProcessor.getFinalVariables();

    CounterContainer counters = DecompilerContext.getCounterContainer();

    final Map<VarVersion, Integer> mapVarPaar = new HashMap<>();
    Map<Integer, VarVersion> mapOriginalVarIndices = new HashMap<>();
    mapOriginalVarIndices.putAll(this.mapOriginalVarIndices);

    // map var-version pairs on new var indexes
    List<VarVersion> vvps = new ArrayList<>(mapExprentMinTypes.keySet());
    Collections.sort(vvps);

    for (VarVersion pair : vvps) {

      if (pair.version >= 0) {
        int newIndex = pair.version == 1 ? pair.var : counters.getCounterAndIncrement(CounterContainer.VAR_COUNTER);

        VarVersion newVar = new VarVersion(newIndex, 0);

        mapExprentMinTypes.put(newVar, mapExprentMinTypes.get(pair));
        mapExprentMaxTypes.put(newVar, mapExprentMaxTypes.get(pair));

        if (mapFinalVars.containsKey(pair)) {
          mapFinalVars.put(newVar, mapFinalVars.remove(pair));
        }

        mapVarPaar.put(pair, newIndex);
        mapOriginalVarIndices.put(newIndex, pair);
      }
    }

    // set new vars
    graph.iterateExprents(exprent -> {
      List<Exprent> lst = exprent.getAllExprents(true);
      lst.add(exprent);

      for (Exprent expr : lst) {
        if (expr.type == Exprent.EXPRENT_VAR) {
          VarExprent newVar = (VarExprent)expr;
          Integer newVarIndex = mapVarPaar.get(new VarVersion(newVar));
          if (newVarIndex != null) {
            String name = newVar.getProcessor().getAssignedVarName(new VarVersion(newVar.getIndex(), 0));
            newVar.setIndex(newVarIndex);
            newVar.setVersion(0);
            if (name != null && newVar.getLVTEntry() == null && newVar.getProcessor().getVarName(newVar.getVarVersion()) == null) {
              newVar.getProcessor().setAssignedVarName(newVar.getVarVersion(), name);
              newVar.getProcessor().setVarName(newVar.getVarVersion(), name);
            }
          }
        }
        else if (expr.type == Exprent.EXPRENT_CONST) {
          VarType maxType = mapExprentMaxTypes.get(new VarVersion(expr.id, -1));
          if (maxType != null && maxType.equals(VarType.VARTYPE_CHAR)) {
            ((ConstExprent)expr).setConstType(maxType);
          }
        }
      }

      return 0;
    });

    if (previousVersionsProcessor != null) {
      Map<Integer, VarVersion> oldIndices = previousVersionsProcessor.getMapOriginalVarIndices();
      this.mapOriginalVarIndices = new HashMap<>(mapOriginalVarIndices.size());
      for (Entry<Integer, VarVersion> entry : mapOriginalVarIndices.entrySet()) {
        VarVersion value = entry.getValue();
        VarVersion oldValue = oldIndices.get(value.var);
        value = oldValue != null ? oldValue : value;
        this.mapOriginalVarIndices.put(entry.getKey(), value);
      }
    }
    else {
      this.mapOriginalVarIndices = mapOriginalVarIndices;
    }
  }

  public VarType getVarType(VarVersion pair) {
    return typeProcessor.getVarType(pair);
  }

  public void setVarType(VarVersion pair, VarType type) {
    typeProcessor.setVarType(pair, type);
  }

  public int getVarFinal(VarVersion pair) {
    Integer fin = typeProcessor.getFinalVariables().get(pair);
    return fin == null ? VarProcessor.VAR_FINAL : fin;
  }

  public void setVarFinal(VarVersion pair, int finalType) {
    typeProcessor.getFinalVariables().put(pair, finalType);
  }

  public Map<Integer, VarVersion> getMapOriginalVarIndices() {
    return mapOriginalVarIndices;
  }

  public VarTypeProcessor getTypeProcessor() {
    return typeProcessor;
  }
}