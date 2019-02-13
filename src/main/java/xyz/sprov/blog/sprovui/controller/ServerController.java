package xyz.sprov.blog.sprovui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.sprov.blog.sprovui.exception.V2rayException;
import xyz.sprov.blog.sprovui.bean.Msg;
import xyz.sprov.blog.sprovui.service.ServerService;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("server")
public class ServerController {

    @Autowired
    private ServerService service;

    @ResponseBody
    @PostMapping("status")
    public Msg status(HttpServletRequest request) {
        try {
            return new Msg(true, service.statuses(request));
        } catch (V2rayException e) {
            return new Msg(false, e.getMessage(), e.getObject());
        } catch (Exception e) {
            return new Msg(false, e.getMessage());
        }
    }

}
