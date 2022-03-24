package entity;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class User extends BaseEntity{
    private String name;
    private String second_name;

    public User(Long id, String name, String second_name){
        super(id);
        this.name = name;
        this.second_name = second_name;
    }

}
