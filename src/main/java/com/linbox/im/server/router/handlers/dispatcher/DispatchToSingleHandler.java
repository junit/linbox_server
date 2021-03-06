package com.linbox.im.server.router.handlers.dispatcher;

import com.alibaba.fastjson.JSON;
import com.linbox.im.exceptions.IMConsumerException;
import com.linbox.im.exceptions.IMException;
import com.linbox.im.message.MessageType;
import com.linbox.im.message.NewMessage;
import com.linbox.im.server.service.IInboxService;
import com.linbox.im.server.service.IOutboxService;
import com.linbox.im.message.Message;
import com.linbox.im.server.router.handlers.Handler;
import com.linbox.im.server.storage.dao.IUserDAO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Created by lrsec on 8/26/15.
 */
@Service
@Qualifier("dispatchToSingleHandler")
public class DispatchToSingleHandler implements Handler<String, String> {
    private static final Logger logger = LoggerFactory.getLogger(DispatchToSingleHandler.class);

    @Autowired
    private IInboxService inboxService;

    @Autowired
    private IOutboxService outboxService;

    @Autowired
    private IUserDAO userDAO;

    @Override
    public void handle(ConsumerRecord<String, String> record) {
        String json = record.value();

        try {
            SendDispatchMessage dispatchMsg = JSON.parseObject(json, SendDispatchMessage.class);
            if (dispatchMsg == null) {
                throw new IMException("DispatchMessage could not be parsed correctly. Message: " + json);
            }

            MessageType type = dispatchMsg.getType();

            switch (type) {
                case Session:
                    dealSingleMessage(dispatchMsg);
                    break;
                case Group:
                    dealGroupMessage(dispatchMsg);
                    break;
                default:
                    throw new IMException("Get an unknown message type " + type.name() + " for SingleSendDispatchCallback");
            }
        } catch (Exception e) {
            throw new IMConsumerException(e, json);
        }
    }

    private void dealSingleMessage(SendDispatchMessage dispatchMessage) {
        String userId = dispatchMessage.getUserId();
        String remoteId = dispatchMessage.getRemoteId();
        String sessionKey = dispatchMessage.getSessionKey();
        Message message = dispatchMessage.getMessage();

        inboxService.updateSessionMsg(userId, sessionKey, message);
        sendNewMsgToUser(userId, remoteId, MessageType.Session);
        sendPush(message);
    }

    private void dealGroupMessage(SendDispatchMessage dispatchMessage) {
        String userId = dispatchMessage.getUserId();
        String remoteId = dispatchMessage.getRemoteId();
        String sessionKey = dispatchMessage.getSessionKey();
        Message message = dispatchMessage.getMessage();

        inboxService.updateGroupMsg(userId, sessionKey, message);
        sendNewMsgToUser(userId, remoteId, MessageType.Group);
        sendConsultPush(userId, message);
    }

    private void sendNewMsgToUser(String userId, String remoteId, MessageType type) {
        NewMessage newMessage = new NewMessage();

        newMessage.userId = userId;
        newMessage.remoteId = remoteId;

        newMessage.type = type.getValue();

        if (type == MessageType.Group) {
            newMessage.groupId = remoteId;
        }

        outboxService.put(userId, newMessage.toWrapperJson());
    }

    private void sendPush(Message message) {
//        String fromUserId = message.fromUserId;
//        String fromUserName = userDAO.getUserName(fromUserId);
//        String toUserId = message.toUserId;

//        PushMessage pushMessage = new PushMessage();
//
//        if (fromUserId.equalsIgnoreCase("10000")) {
//            pushMessage.Type = SystemMsgType.IM_SYSTEM_ASSISTANT;
//        } else {
//            pushMessage.Type = SystemMsgType.IM_SESSION;
//        }
//
//        pushMessage.ActionType = MessageAction.Text;
//
//        pushMessage.From = Long.parseLong(fromUserId);
//        pushMessage.To = Long.parseLong(toUserId);
//        pushMessage.Badge = ServiceFactory.getInboxService().getTotalUnreadCount(toUserId) + 1;
//
//        if (message.mimeType.startsWith("text")) {
//            pushMessage.Description = fromUserName + ": " + message.content;
//            pushMessage.Message = fromUserName + ": " + message.content;
//        } else if (message.mimeType.startsWith("audio")) {
//            pushMessage.Description = fromUserName + ": " + "[发来一条语音消息]";
//            pushMessage.Message = fromUserName + ": " + "[发来一条语音消息]";
//        } else if (message.mimeType.startsWith("image")) {
//            pushMessage.Description = fromUserName + ": " + "[发来一张图片]";
//            pushMessage.Message = fromUserName + ": " + "[发来一张图片]";
//        }
//
//        IPushService pushService = ServiceFactory.getPushService();
//        pushService.sendPush(pushMessage.From, pushMessage);
    }

    private void sendConsultPush(String userId, Message message) {
//        String fromUserId = message.fromUserId;
//        String fromUserName = userDAO.getUserName(fromUserId);

//        PushMessage pushMessage = new PushMessage();
//        pushMessage.ActionType = MessageAction.Text;
//        pushMessage.Type = SystemMsgType.IM_CONSULT;
//
//        pushMessage.From = Long.parseLong(fromUserId);
//        pushMessage.To = Long.parseLong(userId);
//        pushMessage.Badge = ServiceFactory.getInboxService().getTotalUnreadCount(userId) + 1;
//
//        if (message.mimeType.startsWith("text")) {
//            pushMessage.Description = fromUserName + ": " + message.content;
//            pushMessage.Message = fromUserName + ": " + message.content;
//        } else if (message.mimeType.startsWith("audio")) {
//            pushMessage.Description = fromUserName + ": " + "[发来一条语音消息]";
//            pushMessage.Message = fromUserName + ": " + "[发来一条语音消息]";
//        } else if (message.mimeType.startsWith("image")) {
//            pushMessage.Description = fromUserName + ": " + "[发来一张图片]";
//            pushMessage.Message = fromUserName + ": " + "[发来一张图片]";
//        }
//
//        IPushService pushService = ServiceFactory.getPushService();
//        pushService.sendPush(pushMessage.From, pushMessage);
    }
}
