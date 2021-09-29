
package com.umeet.umeet.controller;

import com.umeet.umeet.dtos.UserDto;
import com.umeet.umeet.dtos.UserValidacionDto;
import com.umeet.umeet.entities.User;
import com.umeet.umeet.feign.ProfileFeign;
import com.umeet.umeet.repositories.ProfileRepository;
import com.umeet.umeet.services.FriendService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    @Value("${carpetas.recursos.umeet}")
    private String rutaRecursos; 
    
    @Autowired
    private ModelMapper mapper;
    
    @Autowired
    private ProfileFeign profileFeign;
    
    @Autowired
    ProfileRepository profileRepository;
    
    @Autowired
    FriendService friendService;
    
    //Visualizar los datos del user
    @GetMapping("/view")
    public String view(Model m){
        UserValidacionDto u=(UserValidacionDto)(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        
        User profile = profileFeign.view(u.getId());
        
        m.addAttribute("profile", profile);
        
        return "profile/view";
    }
    
    //Cargar vista con datos del user de la BBDD
    @GetMapping("/edit")
    public String edit(Model m){
        UserValidacionDto u=(UserValidacionDto)(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        User profile = profileFeign.edit(u.getId());
        
        return "editProfile";
    }
    
    //Modifica en la BBDD los datos editados
    @PostMapping("/modify")
    public String modify(Model m, String nickName,String status, String email, MultipartFile avatar){
        UserValidacionDto u=(UserValidacionDto)(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        
        profileFeign.modify(nickName, status, email, avatar, u.getId());
        
        return "redirect:view";
    }
    
    //Obtiene la imagen del avatar del user
    @GetMapping("/avatar")
    public ResponseEntity<Resource> avatar(String url){
        
        HttpHeaders cabeceras=new HttpHeaders();
        cabeceras.add("Content-Disposition", "attachment;");
        cabeceras.add("Cache-Control", "no-cache, no-store, must-revalidate");
        cabeceras.add("Pragma", "no-cache");
        cabeceras.add("Expires", "0");
        
        try{
            return ResponseEntity.ok()
                                 .headers(cabeceras)
                                 .contentLength((new File(url)).length())
                                 .contentType(MediaType.parseMediaType( "application/octet-stream" ))
                                 .body(new InputStreamResource(new FileInputStream( url )) );
        }catch(FileNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }    
    
    //Borra los datos mediante el id del user
    @GetMapping("/remove")
    public String remove(Model m){
        UserValidacionDto u=(UserValidacionDto)(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        
        profileFeign.remove(u.getId());
        return "redirect:logout";
    }
    
    //Modificar estado del user
    @PostMapping("/status")
    public String status(String status){
        UserValidacionDto u=(UserValidacionDto)(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        
        profileFeign.status(u.getId());
        return "redirect:view";
    }
    
    @ResponseBody
    @GetMapping("/statusDrop")
    public void statusDrop(String status){
        UserValidacionDto u=(UserValidacionDto)(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        
        profileFeign.statusDrop(u.getId());
    }
    
    //Obtiene los datos del user
    @GetMapping("/getUser")
    @ResponseBody
    public UserDto getUser(Model m){
        UserValidacionDto u=(UserValidacionDto)(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
         
        UserDto userDto = profileFeign.getUser(u.getId());
        
        return userDto;
    }           
}
