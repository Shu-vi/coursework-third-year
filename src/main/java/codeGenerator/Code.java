package codeGenerator;

public class Code {
    private String code;
    private Generator generator;

    public Code(){
        this.generator = new Generator();
    }

    public String getUniqueCode() {
        code = generator.generate();
        return code;
    }
}
