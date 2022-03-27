package entity;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class User extends BaseEntity{
    private String name;
    private String second_name;
    private String code;

    public User(Long id, String name, String second_name, String code){
        super(id);
        this.name = name;
        this.second_name = second_name;
        this.code = code;
    }

}
