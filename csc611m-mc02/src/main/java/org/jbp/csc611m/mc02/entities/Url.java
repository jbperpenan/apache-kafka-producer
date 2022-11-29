package org.jbp.csc611m.mc02.entities;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "URL")
public class Url {

    @Id
    @GeneratedValue
    private Long id;

    private String url;
    private String worker;
    private String status;

    public Url(String url, String local, String status) {
        this.url = url;
        this.worker = local;
        this.status = status;
    }
}
