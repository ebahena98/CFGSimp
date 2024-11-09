import java.util.*;
import java.io.*;

public class CFGSimp {


    private static Map<String, List<String>> readCFGFile(String filename) throws IOException {
        Map<String, List<String>> grammar = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String text;
        while((text = br.readLine()) != null) {
            String[] productionRules = text.split("-");
            String variable = productionRules[0].trim();
            String[] product = productionRules[1].trim().split("\\|");
            grammar.put(variable, new ArrayList<>(Arrays.asList(product)));
        }

        br.close();
        return grammar;
    } // END OF readCFGFile Method


    private static Map<String, List<String>> removeEpsilon(Map<String, List<String>> grammar) {
        Set<String> lamda = new HashSet<>();
        boolean status = true;

        while (status) {
            status = false;

            for (Map.Entry<String, List<String>> entry : grammar.entrySet()) {
                String variableKey = entry.getKey();
                for (String productions : entry.getValue()) {
                    if (productions.equalsIgnoreCase("lamda") || allLamda(productions, lamda)) {
                        if (lamda.add(variableKey)) {
                            status = true;
                        }
                    }
                }
            }
        }

        Map<String, List<String>> newGrammar = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : grammar.entrySet()) {
            String var = entry.getKey();
            Set<String> newProductions = new HashSet<>();
            for (String productions: entry.getValue()) {
                if (!productions.equalsIgnoreCase("lamda")) {
                    List<String> calibratedProductions = getCalibratedProductions(productions, lamda);
                    newProductions.addAll(calibratedProductions);
                }
            }

            newGrammar.put(var, new ArrayList<>(newProductions));
        }
        
        return newGrammar;
    } // END OF removeEpsilon Method
    private static boolean allLamda(String production, Set<String> lamda) {
        for (char c : production.toCharArray()) {
            if (!lamda.contains(String.valueOf(c))) {
                return false;
            }
        }

        return true;
    }
    private static List<String> getCalibratedProductions(String productions, Set<String> lamda) {
        List<String> calibrated = new ArrayList<>();
        getSubsets(productions, lamda, 0, "", calibrated);
        return calibrated;
    }
    private static void getSubsets(String productions, Set<String> lamda, int index, String current, List<String> results) {
        if (index == productions.length()) {
            if (!current.isEmpty()) {
                results.add(current);
            }

            return;
        }

        char c = productions.charAt(index);
        if (lamda.contains(String.valueOf(c))) {
            getSubsets(productions, lamda, index + 1, current, results);
        }

        getSubsets(productions, lamda, index + 1, current + c, results);
    } //END OF getSubsets


    private static Map<String, List<String>> removeUnitProductions(Map<String, List<String>> grammar) {
        boolean status = true;

        while (status) {
            Map<String, Set<String>> unitProd = new HashMap<>();
            status = false;

            for (Map.Entry<String, List<String>> entry : grammar.entrySet()) {
                String var = entry.getKey();
                Set<String> creationSet = new HashSet<>();
                for (String productions : entry.getValue()) {
                    if (productions.length() == 1 && Character.isUpperCase(productions.charAt(0))) {
                        creationSet.add(productions);
                    }
                }

                unitProd.put(var, creationSet);
            }

            for (Map.Entry<String, Set<String>> entry : unitProd.entrySet()) {
                String var = entry.getKey();
                Set<String> creationSet = entry.getValue();
                List<String> newProductions = new ArrayList<>(grammar.get(var));

                for (String creation : creationSet) {
                    List<String> unitProdList = grammar.get(creation);
                    for (String unit : unitProdList) {
                        if (!newProductions.contains(unit)) {
                            newProductions.add(unit);
                        }
                    }
                }

                if (newProductions.size() > grammar.get(var).size()) {
                    grammar.put(var, newProductions);
                    status = true;
                }
            }
        }

        return grammar;
    } // END OF REMOVE UNIT PRODUCTIONS METHOD


    private static Map<String, List<String>> removeUseless(Map<String, List<String>> grammar) {
        Set<String> creationSet = new HashSet<>();
        boolean status = true;

        while (status) {
            status = false;
            for (Map.Entry<String, List<String>> entry : grammar.entrySet()) {
                String var = entry.getKey();
                for (String productions : entry.getValue()) {
                    if (terminalsOrCreations(productions, creationSet)) {
                        if (creationSet.add(var)) {
                            status = true;
                        }
                    }
                }
            }
        }

        Map<String, List<String>> newGrammar = new HashMap<>();
        for (String variable : creationSet) {
            Set<String> newProductions = new HashSet<>();
            for (String productions : grammar.get(variable)) {
                if (terminalsOrCreations(productions, creationSet)) {
                    newProductions.add(productions);
                }
            }

            newGrammar.put(variable, new ArrayList<>(newProductions));
        }

        return newGrammar;
    } // END OF removeUseless Method
    private static boolean terminalsOrCreations(String productions, Set<String> creationSet) {
        for (char c : productions.toCharArray()) {
            if (!Character.isLowerCase(c) && !creationSet.contains(String.valueOf(c))) {
                return false;
            }
        }

        return true;
    } // End of terminalsOrCreations Method

    
    private static Map<String, List<String>> removeOwnProductions(Map<String, List<String>> grammar, String startSymbol) {
        Map<String, List<String>> newGrammar = new HashMap<>(grammar);
        if (newGrammar.containsKey(startSymbol)) {
            newGrammar.get(startSymbol).remove(startSymbol);
        }

        return newGrammar;
    } // END OF REMOVE Own PRODUCTION


    private static void printCFG(Map<String, List<String>> grammar) {
        List<String> variables = new ArrayList<>(grammar.keySet());
        Collections.sort(variables);

        if (variables.remove("S")) {
            variables.add(0, "S");
        }

        for (String variable : variables) {
            List<String> productions = grammar.get(variable);
            List<String> terminals = new ArrayList<>();
            List<String> both = new ArrayList<>();

            for (String prod : productions) {
                if (prod.chars().allMatch(Character::isLowerCase)) {
                    terminals.add(prod);
                } else {
                    both.add(prod);
                }
            }

            both.addAll(terminals);
            System.out.print(variable + "->");
            System.out.println(String.join("|", both));
        }
    }
    public static void main(String[] args) {

        try {
            Scanner scnr = new Scanner(System.in);
            // System.out.print("Enter file name for CFG to simplify: ");
            // String filename = scnr.nextLine();
            String filename = "cfg.txt";
            Map<String, List<String>> grammar = readCFGFile(filename);
            System.out.println();

            System.out.println("Given Grammar");
            printCFG(grammar);

            grammar = removeEpsilon(grammar);
            System.out.println("\nRemove Epsilon Grammar");
            printCFG(grammar);

            System.out.println("\nRemove Unit Production Grammar");
            grammar = removeUnitProductions(grammar);
            printCFG(grammar);

            System.out.println("\nRemove Useless Grammar");
            grammar = removeUseless(grammar);
            printCFG(grammar);

            // System.out.println("\nRemove Own Production Grammar");
            // printCFG(removeOwnProductions(grammar, "S"));
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    } // END OF STATIC MAIN METHOD


} // END OF CFGSIMP CLASS