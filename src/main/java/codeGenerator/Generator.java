package codeGenerator;

import database.DatabaseControl;
import entity.User;

import java.util.UUID;

public class Generator {
    private byte codeLength;
    private DatabaseControl databaseControl;

    Generator(){
        this.codeLength = 6;
        databaseControl = DatabaseControl.getObjectDatabaseControl();
    }

    public String generate(){
        String code = null;
        Boolean flag = false;
        while (!flag){
            code = UUID.randomUUID().toString().substring(0, this.codeLength);
            flag = isCodeUnique(code);
        }
        return code;
    }

    private boolean isCodeUnique(String code){
        User user = databaseControl.getUserByCode(code);
        return user==null;
    }
}
