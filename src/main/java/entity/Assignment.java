package entity;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.*;

@Getter @Setter
public class Assignment extends BaseEntity{
    private Long idUserFrom;
    private Long idUserTo;
    private Long idGroupTo;
    private String detail;
    private DateTime date;
    private String report;
    private String edits;
    private Boolean done;
    private Boolean open;


    //Если поручение пользователю
    public Assignment(Long id, Long idUserFrom, Long idUserTo, String detail, DateTime date, String report, String edits, Boolean done, Boolean open) {
        super(id);
        this.idUserFrom = idUserFrom;
        this.idUserTo = idUserTo;
        this.detail = detail;
        this.date = date;
        this.report = report;
        this.edits = edits;
        this.done = done;
        this.open = open;
    }

    //Если поручение группе
    public Assignment(Long id, Long idUserFrom, Long idUserTo, String detail, DateTime date, String report, String edits, Boolean done, Long idGroupTo, Boolean open){
        super(id);
        this.idUserFrom = idUserFrom;
        this.detail = detail;
        this.date = date;
        this.report = report;
        this.edits = edits;
        this.done = done;
        this.idGroupTo = idGroupTo;
        this.open = open;
        this.idUserTo = idUserTo;
    }
}