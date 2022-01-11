package main.visitor.codeGenerator;

import main.ast.nodes.*;
import main.ast.nodes.declaration.*;
import main.ast.nodes.declaration.struct.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.*;
import main.ast.nodes.expression.values.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.nodes.statement.*;
import main.ast.types.*;
import main.ast.types.primitives.*;
import main.symbolTable.*;
import main.symbolTable.exceptions.*;
import main.symbolTable.items.FunctionSymbolTableItem;
import main.symbolTable.items.StructSymbolTableItem;
import main.visitor.Visitor;
import main.visitor.type.ExpressionTypeChecker;
import java.io.*;
import java.util.*;

public class CodeGenerator extends Visitor<String> {
    ExpressionTypeChecker expressionTypeChecker = new ExpressionTypeChecker();
    private String outputPath;
    private FileWriter currentFile;

    private Boolean isInStruct = false;
    private ArrayList<String> arr = new ArrayList<>();
    private int label = 0;

    private void copyFile(String toBeCopied, String toBePasted) {
        try {
            File readingFile = new File(toBeCopied);
            File writingFile = new File(toBePasted);
            InputStream readingFileStream = new FileInputStream(readingFile);
            OutputStream writingFileStream = new FileOutputStream(writingFile);
            byte[] buffer = new byte[1024];
            int readLength;
            while ((readLength = readingFileStream.read(buffer)) > 0)
                writingFileStream.write(buffer, 0, readLength);
            readingFileStream.close();
            writingFileStream.close();
        } catch (IOException e) {//unreachable
        }
    }

    private void prepareOutputFolder() {
        this.outputPath = "output/";
        String jasminPath = "utilities/jarFiles/jasmin.jar";
        String listClassPath = "utilities/codeGenerationUtilityClasses/List.j";
        String fptrClassPath = "utilities/codeGenerationUtilityClasses/Fptr.j";
        try {
            File directory = new File(this.outputPath);
            File[] files = directory.listFiles();
            if(files != null)
                for (File file : files)
                    file.delete();
            directory.mkdir();
        }
        catch(SecurityException e) {//unreachable

        }
        copyFile(jasminPath, this.outputPath + "jasmin.jar");
        copyFile(listClassPath, this.outputPath + "List.j");
        copyFile(fptrClassPath, this.outputPath + "Fptr.j");
    }

    private void createFile(String name) {
        try {
            String path = this.outputPath + name + ".j";
            File file = new File(path);
            file.createNewFile();
            this.currentFile = new FileWriter(path);
        } catch (IOException e) {//never reached
        }
    }

    private void addCommand(String command) {
        try {
            command = String.join("\n\t\t", command.split("\n"));
            if(command.startsWith("Label_"))
                this.currentFile.write("\t" + command + "\n");
            else if(command.startsWith("."))
                this.currentFile.write(command + "\n");
            else
                this.currentFile.write("\t\t" + command + "\n");
            this.currentFile.flush();
        } catch (IOException e) {//unreachable

        }
    }

    private void addStaticMainMethod(Statement main) {
        addCommand(".method public static main([Ljava/lang/String;)V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        addCommand("new Main");
        addCommand("dup");
        addCommand("invokespecial Main/<init>()V");
        addCommand("astore_0");
        main.accept(this);
        addCommand("return");
        addCommand(".end method");
    }

    private int slotOf(String identifier) {
        if (arr.contains(identifier)) {
            return arr.indexOf(identifier) + 1;
        }
        arr.add(identifier);
        return arr.size();
    }

    private String getJasminType(Type type) {
        if (type instanceof BoolType)
            return "Ljava/lang/Boolean;";
        if (type instanceof IntType)
            return "Ljava/lang/Integer;";
        if (type instanceof ListType)
            return "LList;";
        if (type instanceof FptrType)
            return "LFtpr;";
        if (type instanceof StructType)
            return "L" + ((StructType) type).getStructName() + ";";
        return "V";
    }

    private void setHeaders() {
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
    }

    private void setFooter() {
        addCommand("return");
        addCommand(".end method");
    }

    private String primitiveToNone(Type var) {
        if (var instanceof IntType) {
            return "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;";
        } else if (var instanceof BoolType) {
            return "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;";
        }
        return "\n";
    }

    private String noneToPrimitive(Type var){
        if(var instanceof IntType){
            return "invokevirtual java/lang/Integer/intValue()I";
        }else if(var instanceof BoolType){
            return "invokevirtual java/lang/Boolean/booleanValue()Z\n";
        }
        return "\n";
    }

    private String getType(Type variableType){
        if (variableType instanceof IntType) {
            return "java/lang/Integer";
        }
        if (variableType instanceof BoolType) {
            return "java/lang/Boolean";
        }
        if (variableType instanceof ListType) {
            return  "List";
        }
        if (variableType instanceof FptrType) {
            return "Fptr";
        }
        if (variableType instanceof StructType) {
            StructType struct = (StructType) variableType;
            String nameStruct = struct.getStructName().getName();
            return nameStruct;
        }
        return "";
    }

    private String cast(Type type)
    {
        var typeName = getJasminType(type);
        return "checkcast " + typeName.substring(1, typeName.length() - 1);
    }

    @Override
    public String visit(Program program) {
        prepareOutputFolder();

        isInStruct = true;
        for (StructDeclaration structDeclaration : program.getStructs()) {
            arr.clear();
            label = 0;
            structDeclaration.accept(this);
        }
        isInStruct = false;

        createFile("Main");
        arr.clear();
        label = 0;
        program.getMain().accept(this);

        for (FunctionDeclaration functionDeclaration : program.getFunctions()) {
            arr.clear();
            label = 0;
            functionDeclaration.accept(this);
        }
        return null;
    }

    @Override
    public String visit(StructDeclaration structDeclaration) {
        try {
            String structKey = StructSymbolTableItem.START_KEY + structDeclaration.getStructName().getName();
            StructSymbolTableItem structSymbolTableItem = (StructSymbolTableItem) SymbolTable.root.getItem(structKey);
            SymbolTable.push(structSymbolTableItem.getStructSymbolTable());
        } catch (ItemNotFoundException e) {//unreachable
        }
        createFile(structDeclaration.getStructName().getName());

        //todo - not complete
        addCommand(".class public "+structDeclaration.getStructName().getName());
        addCommand(".super java/lang/Object");

        structDeclaration.getBody().accept(this);
        addCommand(".method public <init>()V");
        setHeaders();
        addCommand("aload_0");
        addCommand("invokespecial java/lang/Object/<init>()V");
        // add default values in constructor
        setFooter();


        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(FunctionDeclaration functionDeclaration) {
        try {
            String functionKey = FunctionSymbolTableItem.START_KEY + functionDeclaration.getFunctionName().getName();
            FunctionSymbolTableItem functionSymbolTableItem = (FunctionSymbolTableItem) SymbolTable.root.getItem(functionKey);
            SymbolTable.push(functionSymbolTableItem.getFunctionSymbolTable());
        } catch (ItemNotFoundException e) {//unreachable
        }
        //todo - check
        StringBuilder prototype = new StringBuilder(".method public " + functionDeclaration.getFunctionName().getName() + "(");
        for (VariableDeclaration arg : functionDeclaration.getArgs()) {
            prototype.append(getJasminType(arg.getVarType())); // arguments are none primitive!
        }
        prototype.append(")").append(getJasminType(functionDeclaration.getReturnType()));
        addCommand(prototype.toString());
        setHeaders();
        functionDeclaration.getBody().accept(this);
        addCommand(".end method");
        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(MainDeclaration mainDeclaration) {
        try {
            String functionKey = FunctionSymbolTableItem.START_KEY + "main";
            FunctionSymbolTableItem functionSymbolTableItem = (FunctionSymbolTableItem) SymbolTable.root.getItem(functionKey);
            SymbolTable.push(functionSymbolTableItem.getFunctionSymbolTable());
        } catch (ItemNotFoundException e) {//unreachable
        }

        addCommand(".class public Main");
        addCommand(".super java/lang/Object");
        addCommand(".method public <init>()V");
        setHeaders();
        addCommand("aload_0");
        addCommand("invokespecial java/lang/Object/<init>()V");
        setFooter();
        addStaticMainMethod(mainDeclaration.getBody());
        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(VariableDeclaration variableDeclaration) {
        //todo
        Type variableType = variableDeclaration.getVarType();
        if (isInStruct) {
            if (variableType instanceof IntType) {
                addCommand(".field public " + variableDeclaration.getVarName().getName() + " Ljava/lang/Integer;");
            }
            if (variableType instanceof BoolType) {
                addCommand(".field public " + variableDeclaration.getVarName().getName() + " Ljava/lang/Boolean;");
            }
            if (variableType instanceof ListType) {
                addCommand(".field public " + variableDeclaration.getVarName().getName() + " LList;");
            }
            if (variableType instanceof FptrType) {
                addCommand(".field public " + variableDeclaration.getVarName().getName() + " LFptr;");
            }
            if (variableType instanceof StructType) {
                StructType struct = (StructType) variableType;
                String nameStruct = struct.getStructName().getName();
                addCommand(".field public " + variableDeclaration.getVarName().getName() + " L" + nameStruct + ";");
            }
        } else {
            if (variableDeclaration.getDefaultValue() != null) {
                addCommand(variableDeclaration.getDefaultValue().accept(this));
            } else {
                if (variableType instanceof IntType || variableType instanceof BoolType) {
                    addCommand("iconst_0");
                    addCommand(primitiveToNone(variableType));
                }
                if (variableType instanceof ListType) {
                    addCommand("new List");
                    addCommand("dup");
                    addCommand("new java/util/ArrayList");
                    addCommand("dup");
                    addCommand("invokespecial java/util/ArrayList/<init>()V");
                    addCommand("invokespecial List/<init>(Ljava/util/ArrayList;)V");
                }
                if (variableType instanceof FptrType) {
                    addCommand("aconst_null");
                }
                if (variableType instanceof StructType) {
                    StructType struct = (StructType) variableType;
                    String nameStruct = struct.getStructName().getName();
                    addCommand("new " + nameStruct);
                    addCommand("dup");
                    addCommand("invokespecial " + nameStruct + "/<init>()V");
                }
            }
            var slotNo = slotOf(variableDeclaration.getVarName().getName());
            addCommand((slotNo > 3 ? "astore " : "astore_") + slotNo);
        }

        return null;
    }

    @Override
    public String visit(SetGetVarDeclaration setGetVarDeclaration) {
        return null;
    }

    private boolean isInAssignmentStmt = false;
    @Override
    public String visit(AssignmentStmt assignmentStmt) {
        //todo
        BinaryExpression node = new BinaryExpression(assignmentStmt.getLValue(),assignmentStmt.getRValue(),BinaryOperator.assign);
        isInAssignmentStmt = true;
        addCommand(node.accept(this));
        isInAssignmentStmt = false;
        addCommand("pop");
        return null;
    }

    @Override
    public String visit(BlockStmt blockStmt) {
        for (Statement stmt : blockStmt.getStatements()) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public String visit(ConditionalStmt conditionalStmt) {
        //todo
        return null;
    }

    @Override
    public String visit(FunctionCallStmt functionCallStmt) {
        //todo - check -- same as pdf
        expressionTypeChecker.setInFunctionCallStmt(true);
        addCommand(functionCallStmt.getFunctionCall().accept(this));
        addCommand("pop");
        expressionTypeChecker.setInFunctionCallStmt(false);
        return null;
    }

    @Override
    public String visit(DisplayStmt displayStmt) {
        addCommand("getstatic java/lang/System/out Ljava/io/PrintStream;");
        Type argType = displayStmt.getArg().accept(expressionTypeChecker);
        String commandsOfArg = displayStmt.getArg().accept(this);
        addCommand(commandsOfArg);
        addCommand(noneToPrimitive(argType));
        if (argType instanceof IntType)
            addCommand("invokevirtual java/io/PrintStream/println(I)V");
        if (argType instanceof BoolType)
            addCommand("invokevirtual java/io/PrintStream/println(Z)V");

        return null;
    }

    @Override
    public String visit(ReturnStmt returnStmt) {
        //todo - check -- same as pdf
        if (returnStmt.getReturnedExpr() != null) {
            addCommand(returnStmt.getReturnedExpr().accept(this));
            addCommand("areturn");
        } else {
            addCommand("return");
        }
        return null;
    }

    @Override
    public String visit(LoopStmt loopStmt) {
        //todo
        return null;
    }

    @Override
    public String visit(VarDecStmt varDecStmt) {
        for (VariableDeclaration stmt : varDecStmt.getVars()) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public String visit(ListAppendStmt listAppendStmt) {
        //todo - icheck -- same as pdf
        expressionTypeChecker.setInFunctionCallStmt(true);
        addCommand(listAppendStmt.getListAppendExpr().accept(this));
        expressionTypeChecker.setInFunctionCallStmt(false);
        return null;
    }

    @Override
    public String visit(ListSizeStmt listSizeStmt) {
        // todo - icheck -- same as pdf
        addCommand(listSizeStmt.getListSizeExpr().accept(this));
        addCommand("pop");
        return null;
    }

    @Override
    public String visit(BinaryExpression binaryExpression) {
        //todo
        var sb = new StringBuilder();
        Type expr = binaryExpression.accept(expressionTypeChecker);
        BinaryOperator opr = binaryExpression.getBinaryOperator();
        Type rvalue = binaryExpression.getSecondOperand().accept(expressionTypeChecker); // for list assign
        if(expr instanceof IntType){
            sb.append(binaryExpression.getSecondOperand().accept(this));
            if(opr != BinaryOperator.assign){
                sb.append("\ninvokevirtual java/lang/Integer/intValue()I\n");
                sb.append(binaryExpression.getFirstOperand().accept(this));
                sb.append("\ninvokevirtual java/lang/Integer/intValue()I\n");
            }
            if(opr == BinaryOperator.add){
                sb.append("iadd");
                sb.append("\ndup\n"+primitiveToNone(expr));
                return sb.toString();
            }
            if(opr == BinaryOperator.sub){
                sb.append("isub");
                sb.append("\ndup\n"+primitiveToNone(expr));
            }
            if(opr == BinaryOperator.mult){
                sb.append("imul");
                sb.append("\ndup\n"+primitiveToNone(expr));
            }
            if(opr == BinaryOperator.div){
                sb.append("idiv");
                sb.append("\ndup\n"+primitiveToNone(expr));
            }
            if(opr == BinaryOperator.assign){
                sb.append("\ndup\n");
                Identifier lvalue = (Identifier)binaryExpression.getFirstOperand();
                if(isInAssignmentStmt) {
                    var slotno = slotOf(lvalue.getName());
                    sb.append((slotno > 3 ? "astore " : "astore_") + slotno);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String visit(UnaryExpression unaryExpression) {
        return null;
    }

    @Override
    public String visit(StructAccess structAccess) {
        //todo - check -- same as pdf
        Type obj = structAccess.getInstance().accept(expressionTypeChecker);
        StructType struct = (StructType) obj;
        String nameStruct = struct.getStructName().getName();
        String nameField = structAccess.getElement().getName();
        Type typeField = structAccess.accept(expressionTypeChecker);
        var sb = new StringBuilder();
        sb.append(structAccess.getInstance().accept(this));
        sb.append("\n");
        sb.append("getfield "+nameStruct+"/"+nameField+" L"+getType(typeField)+";");
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String visit(Identifier identifier) {
        Type idType = expressionTypeChecker.visit(identifier);
        if (idType instanceof FptrType) {
            if (!arr.contains(identifier.getName())) {
                addCommand("new Fptr");
                addCommand("dup");
                addCommand("aload_0");
                addCommand("ldc \""+identifier.getName()+"\"");
                addCommand("invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V");
                var createSlotNo = slotOf(identifier.getName());
                addCommand((createSlotNo > 3 ? "astore " : "astore_") + createSlotNo);
            }
        }
        var slotNo = slotOf(identifier.getName());
        addCommand((slotNo > 3 ? "aload " : "aload_") + slotNo);
        return idType instanceof FptrType ? "invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;\n"
                + cast(((FptrType)idType).getReturnType()) + "\n"
                : "";
    }

    @Override
    public String visit(ListAccessByIndex listAccessByIndex) { // return None primitive
        //todo - check -- same as pdf
        var sb = new StringBuilder();
        sb.append(listAccessByIndex.getIndex().accept(this));
        sb.append("\n");
        sb.append("invokevirtual java/lang/Integer/intValue()I");
        sb.append("\n");
        sb.append(listAccessByIndex.getInstance().accept(this));
        sb.append("\n");
        sb.append("invokevirtual List/getElement(I)Ljava/lang/Object");
        sb.append("\n");
        // cast to type
        Type obj = listAccessByIndex.accept(expressionTypeChecker);
        sb.append("checkcast "+ getType(obj));
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String visit(FunctionCall functionCall) {
        var func = functionCall.getInstance().accept(this);
        addCommand("new java/util/ArrayList");
        addCommand("dup");
        addCommand("invokespecial java/util/ArrayList/<init>()V");
        for (Expression arg : functionCall.getArgs()) {
            addCommand("dup");
            addCommand(arg.accept(this));
            addCommand("invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z");
            addCommand("pop");
        }
        return func;
    }

    @Override
    public String visit(ListSize listSize) {
        //todo - check -- same as pdf
        var sb = new StringBuilder();
        sb.append(listSize.getArg().accept(this));
        sb.append("\n");
        sb.append("invokevirtual List/getSize()I\n");
        sb.append(primitiveToNone(new IntType()));
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String visit(ListAppend listAppend) {// return void
        // todo - check -- same as pdf
        var sb = new StringBuilder();
        sb.append(listAppend.getListArg().accept(this));
        sb.append("\n");
        sb.append(listAppend.getElementArg().accept(this));
        sb.append("\n");
        sb.append("invokevirtual List/addElement(Ljava/lang/Object;)V\n");
        return sb.toString();
    }

    @Override
    public String visit(IntValue intValue) { // return none primitive
        return "ldc " + intValue.getConstant() + "\n" + primitiveToNone(new IntType());
    }

    @Override
    public String visit(BoolValue boolValue) { // return none primitive
        return (boolValue.getConstant() ? "ldc 1" : "ldc 0") + "\n" + primitiveToNone(new BoolType());
    }

    @Override
    public String visit(ExprInPar exprInPar) {
        return exprInPar.getInputs().get(0).accept(this);
    }
}
