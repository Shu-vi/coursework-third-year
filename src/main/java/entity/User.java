package entity;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class User extends BaseEntity{
    private String name;
    private String second_name;
    private Integer chat_id;

    public User(Integer id, String name, String second_name, Integer chat_id){
        super(id);
        this.name = name;
        this.second_name = second_name;
        this.chat_id = chat_id;
    }

}
