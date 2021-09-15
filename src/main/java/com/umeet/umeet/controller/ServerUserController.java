/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umeet.umeet.controller;

import com.umeet.umeet.entities.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.umeet.umeet.repositories.ServersRepository;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/servers")
public class ServerUserController {

    @Autowired
    private ServersRepository repoServer;

    //mostrar todos los servidores
    @GetMapping
    public String allServers(Model m) {

        m.addAttribute("server", repoServer.findAll());

        return "/servers/server";
    }

    @PostMapping("/filtered")
    public String searchServer(Model m, String description) {

        m.addAttribute("server", repoServer.findByDescriptionContaining(description));
        return "servers/filteredServers";
    }

}
