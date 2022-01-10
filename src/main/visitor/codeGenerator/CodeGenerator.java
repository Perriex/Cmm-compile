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

public class  CodeGenerator extends Visitor<String> {
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
        try{
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

    private void addStaticMainMethod() {
        addCommand(".method public static main([Ljava/lang/String;)V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        addCommand("new Main");
        addCommand("invokespecial Main/<init>()V");
        addCommand("return");
        addCommand(".end method");
    }

    private int slotOf(String identifier) {
        //todo - done
        if(identifier.equals("")){
            arr.add("");
            return arr.size();
        }
        if(arr.contains(identifier)){
            return arr.indexOf(identifier)+1;
        }
        arr.add(identifier);
        return arr.size();
    }

    private void setHeaders()
    {
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
    }

    private void setFooter() {
        addCommand("return");
        addCommand(".end method");
    }

    @Override
    public String visit(Program program) {
        prepareOutputFolder();

        isInStruct = true;
        for(StructDeclaration structDeclaration : program.getStructs()){
            arr.clear();
            structDeclaration.accept(this);
        }
        isInStruct = false;

        createFile("Main");
        arr.clear();
        program.getMain().accept(this);

        for (FunctionDeclaration functionDeclaration: program.getFunctions()){
            arr.clear();
            functionDeclaration.accept(this);
        }
        return null;
    }

    @Override
    public String visit(StructDeclaration structDeclaration) {
        try{
            String structKey = StructSymbolTableItem.START_KEY + structDeclaration.getStructName().getName();
            StructSymbolTableItem structSymbolTableItem = (StructSymbolTableItem)SymbolTable.root.getItem(structKey);
            SymbolTable.push(structSymbolTableItem.getStructSymbolTable());
        }catch (ItemNotFoundException e){//unreachable
        }
        createFile(structDeclaration.getStructName().getName());

        //todo

        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(FunctionDeclaration functionDeclaration) {
        try{
            String functionKey = FunctionSymbolTableItem.START_KEY + functionDeclaration.getFunctionName().getName();
            FunctionSymbolTableItem functionSymbolTableItem = (FunctionSymbolTableItem)SymbolTable.root.getItem(functionKey);
            SymbolTable.push(functionSymbolTableItem.getFunctionSymbolTable());
        }catch (ItemNotFoundException e){//unreachable
        }

        //todo

        SymbolTable.pop();
        return null;
    }
    @Override
    public String visit(MainDeclaration mainDeclaration) {
        try{
            String functionKey = FunctionSymbolTableItem.START_KEY + "main";
            FunctionSymbolTableItem functionSymbolTableItem = (FunctionSymbolTableItem)SymbolTable.root.getItem(functionKey);
            SymbolTable.push(functionSymbolTableItem.getFunctionSymbolTable());
        }catch (ItemNotFoundException e){//unreachable
        }
        // done
        addCommand(".class public Main");
        addCommand(".super java/lang/Object");
        addStaticMainMethod();
        addCommand(".method public <init>()V");
        setHeaders();
        addCommand("aload_0");
        addCommand("invokespecial java/lang/Object/<init>()V");
        mainDeclaration.getBody().accept(this);
        setFooter();
        //end
        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(VariableDeclaration variableDeclaration) {
        //todo
        Type variableType = variableDeclaration.getVarType();
        if(isInStruct){

        }
        else{
            boolean hasDefault= false;
            if(variableDeclaration.getDefaultValue() != null){
                addCommand(variableDeclaration.getDefaultValue().accept(this));
                hasDefault = true;
            }
            if(variableDeclaration.getVarType() instanceof IntType) {
                if(!hasDefault){
                    addCommand("iconst_0");
                }
                addCommand("invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;");
            }
            if(variableDeclaration.getVarType() instanceof BoolType){
                if(!hasDefault){
                    addCommand("iconst_0");
                }
                addCommand("invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;");
            }
            if(variableDeclaration.getVarType() instanceof ListType){
                addCommand("new List");
                addCommand("dup");
                addCommand("new java/util/ArrayList");
                addCommand("dup");
                addCommand("invokespecial java/util/ArrayList/<init>()V");
                addCommand("invokespecial List/<init>(Ljava/util/ArrayList;)V");
            }
            var slotno = slotOf(variableDeclaration.getVarName().getName());
            addCommand((slotno > 3 ? "astore " : "astore_") + slotno);
        }
        return null;
    }

    @Override
    public String visit(SetGetVarDeclaration setGetVarDeclaration) {
        return null;
    }

    @Override
    public String visit(AssignmentStmt assignmentStmt) {
        //todo - need test ??
        BinaryExpression node = new BinaryExpression(assignmentStmt.getLValue(),
                assignmentStmt.getRValue(),
                BinaryOperator.assign);
        addCommand(node.accept(this));
        addCommand("pop");
        return null;
    }

    @Override
    public String visit(BlockStmt blockStmt) {
        //done
        for(Statement stmt: blockStmt.getStatements()){
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
        //todo
        return null;
    }

    @Override
    public String visit(DisplayStmt displayStmt) {
        addCommand("getstatic java/lang/System/out Ljava/io/PrintStream;");
        Type argType = displayStmt.getArg().accept(expressionTypeChecker);
        String commandsOfArg = displayStmt.getArg().accept(this);

        addCommand(commandsOfArg);
        if (argType instanceof IntType)
            addCommand("invokevirtual java/io/PrintStream/println(I)V");
        if (argType instanceof BoolType)
            addCommand("invokevirtual java/io/PrintStream/println(Z)V");

        return null;
    }

    @Override
    public String visit(ReturnStmt returnStmt) {
        //todo
        return null;
    }

    @Override
    public String visit(LoopStmt loopStmt) {
        //todo
        return null;
    }

    @Override
    public String visit(VarDecStmt varDecStmt) {
        //done
        for(VariableDeclaration stmt: varDecStmt.getVars()){
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public String visit(ListAppendStmt listAppendStmt) {
        //done
        addCommand(listAppendStmt.getListAppendExpr().accept(this));
        return null;
    }

    @Override
    public String visit(ListSizeStmt listSizeStmt) {
        // done
        addCommand(listSizeStmt.getListSizeExpr().accept(this));
        return null;
    }

    @Override
    public String visit(BinaryExpression binaryExpression) {
        //todo ???
        Type expr = expressionTypeChecker.visit(binaryExpression);
        if(expr instanceof IntType){
            String intConv = "\ndup\ninvokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;";
            addCommand(binaryExpression.getFirstOperand().accept(this)+'\n'+
                    "invokevirtual java/lang/Integer/intValue()I" +'\n'+
                    binaryExpression.getSecondOperand().accept(this) +'\n'+
                    "invokevirtual java/lang/Integer/intValue()I");
            if(binaryExpression.getBinaryOperator() == BinaryOperator.add){
                return "iadd"+intConv;
            }
            if(binaryExpression.getBinaryOperator() == BinaryOperator.sub){
                return "isub"+intConv;
            }
            if(binaryExpression.getBinaryOperator() == BinaryOperator.mult){
                return "imul"+intConv;
            }
            if(binaryExpression.getBinaryOperator() == BinaryOperator.div){
                return "idiv"+intConv;
            }
            if(binaryExpression.getBinaryOperator() == BinaryOperator.assign){
                //do
            }
            // == => if_icmpeq
            // =
        }
        if(expr instanceof BoolType){
            // == => if_icmpeq
            // & | ~ =
            String boolConv = "\ndup\ninvokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;";
            if(binaryExpression.getBinaryOperator() == BinaryOperator.and ||
                    binaryExpression.getBinaryOperator() == BinaryOperator.or){
                addCommand(binaryExpression.getFirstOperand().accept(this)+'\n'+
                        "invokevirtual java/lang/Boolean/booleanValue()Z" +'\n'+
                        binaryExpression.getSecondOperand().accept(this) +'\n'+
                        "invokevirtual java/lang/Boolean/booleanValue()Z");

                return (binaryExpression.getBinaryOperator() == BinaryOperator.and ? "iand" : "ior")+boolConv;
            }
            if(binaryExpression.getBinaryOperator() == BinaryOperator.gt ||
                    binaryExpression.getBinaryOperator() == BinaryOperator.lt){
                addCommand(binaryExpression.getFirstOperand().accept(this)+'\n'+
                        "invokevirtual java/lang/Integer/intValue()I" +'\n'+
                        binaryExpression.getSecondOperand().accept(this) +'\n'+
                        "invokevirtual java/lang/Integer/intValue()I");
                label += 5;
                if(binaryExpression.getBinaryOperator() == BinaryOperator.gt) {
                    addCommand("if_icmple Label" + label);
                }
                else{
                    addCommand("if_icmpge Label"+label);
                }
                label += 3;
                addCommand("ldc 1\ngoto Label"+label);
                addCommand("Label"+(label-3)+":");
                addCommand("ldc 0\nLabel"+label+":");
                return boolConv;
            }
        }
        if(expr instanceof ListType){
            // =
        }
        if(expr instanceof FptrType){
            // == => if_acmpeq
            // =
        }
        if(expr instanceof StructType){
            // == => if_acmpeq
            // =
        }
        return null;
    }

    @Override
    public String visit(UnaryExpression unaryExpression){
        return null;
    }

    @Override
    public String visit(StructAccess structAccess){
        //todo
        return null;
    }

    @Override
    public String visit(Identifier identifier){
        //todo
        Type id = expressionTypeChecker.visit(identifier);
        if(id instanceof FptrType){
            //todo
            return null;
        }
        var slotno = slotOf(identifier.getName());
        return (slotno > 3 ? "aload " : "aload_") + slotno;
    }

    @Override
    public String visit(ListAccessByIndex listAccessByIndex){
        //todo
        return null;
    }

    @Override
    public String visit(FunctionCall functionCall){
        //todo
        ArrayList<Expression> args = new ArrayList<>();

        return null;
    }

    @Override
    public String visit(ListSize listSize){
        //done
        addCommand(listSize.getArg().accept(this));
        return "invokevirtual List/getSize()I";
    }

    @Override
    public String visit(ListAppend listAppend) {
        //done
        addCommand(listAppend.getListArg().accept(this));
        addCommand(listAppend.getElementArg().accept(this));
        return "invokevirtual List/addElement(Ljava/lang/Object;)V";
    }

    @Override
    public String visit(IntValue intValue) {
        //done
        return "ldc "+intValue.getConstant() +'\n'+"invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;";
    }

    @Override
    public String visit(BoolValue boolValue) {
        //done
        return (boolValue.getConstant() ? "ldc 1" : "ldc 0" )+ '\n' + "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;";
    }

    @Override
    public String visit(ExprInPar exprInPar) {
        return exprInPar.getInputs().get(0).accept(this);
    }
}
