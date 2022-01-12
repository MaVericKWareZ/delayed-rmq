package com.maverick.delayedrmq.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maverick.delayedrmq.config.QueueNames;
import com.maverick.delayedrmq.dto.UserViewsDTO;
import com.maverick.delayedrmq.service.UserViewsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@RabbitListener
@AllArgsConstructor
@Slf4j
public class UserViewsListener {

    private UserViewsService userViewsService;

    @RabbitListener(queues = QueueNames.USER_VIEWS_QUEUE, containerFactory = "simpleRabbitListenerContainerFactory")
    public void processEvent(String payload) {
        try {
            UserViewsDTO userViewsDTO = new ObjectMapper().convertValue(payload, UserViewsDTO.class);
            userViewsService.processUserViews(userViewsDTO);
        } catch (Exception e) {
            log.error("Error while parsing payload at listener , e={}", e.getMessage());
        }
    }

}
