package entity;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public abstract class BaseEntity {
    protected Long id;

    BaseEntity(Long id){
        this.id = id;
    }

}
