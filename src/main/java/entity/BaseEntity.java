package entity;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public abstract class BaseEntity {
    protected Integer id;

    BaseEntity(Integer id){
        this.id = id;
    }

}
