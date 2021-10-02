package com.umeet.umeet.feign;

import com.umeet.umeet.dtos.MessageChannelDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(contextId = "feign1", name = "UMeetBack", url= "http://localhost:8082")
@RequestMapping("/b/msg")
public interface MessagesFeign {

    @PostMapping("/channel/{id_channel}")
    public List<MessageChannelDto> canales(@PathVariable Long id_channel);
    
    @PostMapping("/private/{id_destino}") 
    public List<MessageChannelDto> privados(@PathVariable Long id_destino, @RequestParam Long idUser);
    
    @PostMapping("/channel/sendmsg") 
    public void mensajeCanal(@RequestParam Long id, @RequestParam String name, @RequestParam String text, @RequestParam Long idChannel,@RequestParam Long idUser);
    
    @PostMapping(value="/channel/sendmsgfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) 
    public void mensajeFileCanal(@RequestPart(value = "file")  MultipartFile file, @RequestParam Long id, @RequestParam Long idUser);
    
    @PostMapping("/private/sendmsg") 
    public void mensajePrivado(@RequestParam Long id, @RequestParam String name, @RequestParam String text, @RequestParam Long idUserDestiny, @RequestParam  Long idUser);
    
    @PostMapping(value="/private/sendmsgfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void mensajeFilePrivado(@RequestPart(value = "file")  MultipartFile file, @RequestParam Long id, @RequestParam  Long idUser);
    
}
