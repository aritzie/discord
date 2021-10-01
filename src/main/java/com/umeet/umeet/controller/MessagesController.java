
package com.umeet.umeet.controller;

import com.umeet.umeet.dtos.MessageChannelDto;
import com.umeet.umeet.dtos.UserDto;
import com.umeet.umeet.entities.Channel;
import com.umeet.umeet.entities.Message;
import com.umeet.umeet.entities.MessageFile;
import com.umeet.umeet.entities.User;
import com.umeet.umeet.repositories.ChannelRepository;
import com.umeet.umeet.repositories.MessageFileRepository;
import com.umeet.umeet.repositories.MessageRepository;
import com.umeet.umeet.repositories.UserRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/b/msg")
public class MessagesController {
    
    @Autowired
    private MessageRepository repoMsg;
   
    @Autowired
    private MessageFileRepository repoMsgFile;
    
    @Autowired
    private ModelMapper mapper;
    
    @Autowired
    private ChannelRepository repoChn;
    
    @Autowired
    private UserRepository repoUsr;
    
    @Value("${carpetas.recursos.umeet}")
    private String rutaRecursos; 
    
    //localhost:8090/msg/channel/1 obtiene todos los mensajes del canal indicado
    @ResponseBody
    @PostMapping("/channel/{id_channel}") //Devuelve un Json con todos los mensajes de un canal
    public List<MessageChannelDto> canales(@PathVariable Long id_channel){
        Optional<Channel> aux = repoChn.findById(id_channel);
        if (aux.isPresent()){
            /*aux.get().getMessages().stream().forEach(
                    x->System.out.println("User:"+(x!=null?x.getUser():"Nada"))
            );*/
            
            List<MessageChannelDto> res = aux.get().getMessages()
                                        .stream()
                                        .map(x->{
                                            if (x.getUser()==null){
                                                System.out.println("Usuarios nulos");
                                            }else{
                                                System.out.print(".");
                                            }
                                            MessageChannelDto msd=mapper.map(x, MessageChannelDto.class);
                                            msd.setUser(mapper.map(x.getUser(), UserDto.class ));
                                            return msd;
                                        })
                                        .collect(Collectors.toList());
           
            return res;
        }
        return null;
    }
    
    //localhost:8090/msg/private/5 (5 seria el usuario que esta obteniendo sus mensajes privados)
    @ResponseBody
    @PostMapping("/private/{id_destino}") //Devuelve un Json con todos los mensajes privados entre el usuario logueado y el usuario destino
    public List<MessageChannelDto> privados(@PathVariable Long id_destino,Long idUser){
        
        List<Message> aux = repoMsg.findByUser(repoUsr.findById(idUser).get());
        if (!aux.isEmpty()){
            List <Message> origen = aux.stream()
                                   .filter(x->x.getUserDestiny()!=null && x.getUserDestiny().getId()==id_destino)
                                   .collect(Collectors.toList());
            if(!repoMsg.findByUserDestiny(repoUsr.findById(id_destino).get()).isEmpty()){
                List<Message> aux2 = repoMsg.findByUser(repoUsr.findById(id_destino).get());
                
                List<Message> destino = aux2.stream()
                          .filter(x-> x.getUserDestiny()!=null && x.getUserDestiny()==repoUsr.findById(idUser).get()) //Obtiene mensajes que el ha recibido
                          .collect(Collectors.toList());
                
                List<MessageChannelDto> res = origen.stream()
                                            .map(x->mapper.map(x, MessageChannelDto.class))
                                            .collect(Collectors.toList());
                res.addAll(destino.stream()
                                .map(x->mapper.map(x, MessageChannelDto.class))
                                .collect(Collectors.toList()));
                res.sort(Comparator.comparing(MessageChannelDto::getId));
                return res;          
            }
        }
        return null;
    }
    
    
    @ResponseBody
    @PostMapping("/channel/sendmsg") //Guarda mensajes en un canal por un usuario
    public void mensajeCanal(Message msg,Long idChannel,Long idUser){
        
        msg.setChannel(repoChn.findById(idChannel).get());
        msg.setUser(repoUsr.findById(idUser).get());
        msg.setName(msg.getUser().getNickName());
        repoMsg.save(msg);
    }
    
    @ResponseBody
    @PostMapping("/channel/sendmsgfile")
    public void mensajeFileCanal(String name, String text, MultipartFile file, Long id, Long idUser){
        User u = repoUsr.findById(idUser).get();
        
        Message msg = new Message();
        
        msg.setChannel(repoChn.findById(id).get());
        msg.setUser(u);
        msg.setName("Fichero");
        msg.setText("Fichero");
        
        mensajeCanal(msg, id, idUser);
        
        
        MessageFile msgFile = new MessageFile();
        
        msgFile.setName("fichero");
        
        String ruta = rutaRecursos + "/file/channel/"+id+"/"+ UUID.randomUUID().toString()+"_"+file.getOriginalFilename();
        File f = new File(ruta);
        f.getParentFile().mkdirs();
        try {
            Files.copy(file.getInputStream(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msgFile.setUrl(ruta);
        msgFile.setMessage(repoMsg.findById(msg.getId()).get());
        
        repoMsgFile.save(msgFile); 
    }
    
    @ResponseBody
    @PostMapping("/private/sendmsg") //Guarda mensajes privados entre usuarios
    public void mensajePrivado(Message msg,Long idUserDestiny, Long idUser){
        User u=repoUsr.findById(idUser).get();
        msg.setName(u.getNickName());
        msg.setUser(u);
        msg.setUserDestiny(repoUsr.findById(idUserDestiny).get());
        repoMsg.save(msg); 
    }
    
    
    @ResponseBody
    @PostMapping("/private/sendmsgfile")
    public void mensajeFilePrivado(String name, String text, MultipartFile file, Long id, Long idUser){
        User u = repoUsr.findById(idUser).get();
        
        Message msg = new Message();
        
        msg.setUserDestiny(repoUsr.findById(id).get());
        msg.setUser(u);
        msg.setName("Fichero");
        msg.setText("Fichero");
        
        mensajePrivado(msg, id, idUser);
        
        
        MessageFile msgFile = new MessageFile();
        
        msgFile.setName("fichero");
        
        String ruta = rutaRecursos + "/file/user/"+idUser+"/"+ UUID.randomUUID().toString()+"_"+file.getOriginalFilename();
        File f = new File(ruta);
        f.getParentFile().mkdirs();
        try {
            Files.copy(file.getInputStream(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msgFile.setUrl(ruta);
        msgFile.setMessage(repoMsg.findById(msg.getId()).get());
        
        repoMsgFile.save(msgFile); 
    }
}
