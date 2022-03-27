package entity;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Group extends BaseEntity{
    private String name;

    public Group(Long id, String name){
        super(id);
        this.name = name;
    }
}
