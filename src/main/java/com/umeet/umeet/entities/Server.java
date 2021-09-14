package com.umeet.umeet.entities;

import jdk.jfr.Enabled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servers")
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String avatar;
    private String status;

    @OneToMany(mappedBy = "server")
    private List<UserServerRole> userServerRole;

    @OneToMany(mappedBy = "server")
    private List<Category> categories;
}
