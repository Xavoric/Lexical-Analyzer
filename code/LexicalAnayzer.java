package code;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LexicalAnayzer {
    private ArrayList<String> keyword;
    private ArrayList<String> operator;
    private ArrayList<Character> monoOP;
    private ArrayList<String> delimiter;

    private BufferedReader br;
    private BufferedWriter bw;

    private String inputFile = "./input.txt";
    private String outputFile = "./output.txt";

    public LexicalAnayzer() throws IOException {
        keyword = new ArrayList<String>(
                List.of("abstract", "assert", "boolean", "break", "byte", "case", "catch",
                        "char", "class", "continue", "default", "do", "double", "else", "enum", "extends",
                        "final", "finally", "float", "for", "if", "implements", "import", "int", "interface",
                        "instanceof", "long", "native", "new", "package", "private", "protected", "public", "return",
                        "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
                        "transient", "try", "void", "volatile", "while", "true", "false", "null", "goto", "const"));

        operator = new ArrayList<String>(
                List.of("+", "-", "*", "/", "%", "++", "--",
                        "=", "+=", "-=", "*=", "/=",
                        ">", "<", ">=", "<=", "==", "!=",
                        "&", "&&", "|", "||"));

        monoOP = new ArrayList<Character>(
                List.of('+', '-', '*', '/','=', '%', '>', '<', '&', '|', '!'));

        delimiter = new ArrayList<String>(
                List.of(";", ",", ".", "(", ")", "[", "]", "{", "}"));

        File input = new File(inputFile);
        FileReader reader = new FileReader(input);
        br = new BufferedReader(reader);

        File output = new File(outputFile);
        FileWriter writer = new FileWriter(output);
        bw = new BufferedWriter(writer);
    }

    public boolean FindInKW(String token) {
        return keyword.contains(token);
    }

    public boolean FindInOP(String token) {
        return operator.contains(token);
    }

    public boolean FindInDE(String token) {
        return delimiter.contains(token);
    }

    public boolean FindInDE(char token) {
        return delimiter.contains("" + token);
    }

    public boolean isLetter(char token) {
        if ((token >= 'a' && token <= 'z') || (token >= 'A' && token <= 'Z') || (token == '_')) {
            return true;
        } else
            return false;
    }

    public boolean isDigit(char token) {
        if (token >= '0' && token <= '9') {
            return true;
        } else
            return false;
    }

    public boolean isOP(char token) {
        return monoOP.contains(token);
    }

    public void printError(String reason) {
        System.out.println("Lexical Error: " + reason);
    }

    public void output(String token, String type) throws IOException {
        System.out.println("< " + token + " , " + type + " >");
        bw.write("< " + token + " , " + type + " >\n");
    }

    public void analyze() throws IOException {
        int c;
        boolean isReadingWord = false;
        boolean isReadingNumber = false;
        boolean isReadingOP = false;
        boolean isReadingString = false;
        String word = "";
        while ((c = br.read()) != -1) {
            // get tokens from input file
            char token = (char) c;
            // Skip white space
            if (Character.isWhitespace(token)) {
                if (word != "") {
                    if (isReadingWord) {
                        if (FindInKW(word)) {
                            output(word, "Keyword");
                        } else {
                            output(word, "Identifier");
                        }
                    } else if (isReadingNumber) {
                        if (word.contains(".")) {
                            output(word, "Decimal");
                        } else {
                            output(word, "Integer");
                        }
                    } else if (isReadingString) {
                        word += token;
                    } else if (isReadingOP){
                        output(word, "Operator");
                    }
                }
                isReadingWord = false;
                isReadingNumber = false;
                isReadingOP = false;
                word = "";
                continue;
            }

            if (isReadingOP) {
                if (isOP(token)) {
                    word += token;
                    continue;
                } else {
                    isReadingOP = false;
                    output(word, "Operator");
                    word = "";
                }
            }

            if (!isReadingWord && !isReadingNumber && !isReadingOP && !isReadingString) {
                if (isLetter(token)) {
                    isReadingWord = true;
                    word += token;
                }
                if (isDigit(token)) {
                    isReadingNumber = true;
                    word += token;
                }
                if (isOP(token)) {
                    isReadingOP = true;
                    word += token;
                }
                if (token == '\"') {
                    isReadingString = true;
                    word += token;
                }
                if (FindInDE(token)) {
                    output("" + token, "Delimiter");
                }
                continue;
            }

            // reading string
            if (isReadingString) {
                if (token == '\"') {
                    isReadingString = false;
                    word += token;
                    output(word, "String");
                    word = "";
                } else {
                    word += token;
                }
            }

            // reading number
            if (isReadingNumber) {
                if (isDigit(token)) {
                    word += token;
                } else if (token == '.') {
                    word += token;
                } else if (isOP(token)) {
                    if (word.contains(".")) {
                        output(word, "Decimal");
                    } else {
                        output(word, "Integer");
                    }
                    word = "";
                    word += token;
                    isReadingNumber = false;
                    isReadingOP = true;
                } else if (FindInDE(token)) {
                    if (word.contains(".")) {
                        output(word, "Decimal");
                    } else {
                        output(word, "Integer");
                    }
                    output("" + token, "Delimiter");
                    word = "";
                    isReadingNumber = false;

                } else {
                    printError("An attribute shouldn't begin with a digit");
                    break;
                }
                continue;
            }

            // reading word
            if (isReadingWord) {
                if (isLetter(token) || isDigit(token)) {
                    word += token;
                } else if (FindInDE(token)) {
                    // meet delimiter
                    if (FindInKW(word)) {
                        output(word, "Keyword");
                    } else {
                        output(word, "Identifier");
                    }
                    output("" + token, "Delimiter");
                    word = "";
                    isReadingWord = false;
                } else if (isOP(token)) {
                    if (FindInKW(word)) {
                        output(word, "Keyword");
                    } else {
                        output(word, "Identifier");
                    }
                    word = "";
                    word += token;
                    isReadingWord = false;
                    isReadingOP = true;
                } else {
                    // A word only includes letter or digit
                    printError("Illegal token");
                    break;
                }
                continue;
            }

        }
        if (isReadingString) {
            printError("Unclosed String\"");
        }

        br.close();
        bw.close();
    }

}
