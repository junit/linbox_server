package com.linbox.im.server.storage.dao;

import com.linbox.im.message.Message;
import com.linbox.im.server.storage.entity.SessionMessageEntity;

import java.util.List;

/**
 * Created by lrsec on 7/2/15.
 */
public interface ISessionMessageDAO {
    SessionMessageEntity insert(Message msg);
    List<SessionMessageEntity> findMsg(String sessioinId, long maxMsgId, long minMsgId, int limit);
    SessionMessageEntity findMsgByRId(long rId, String fromUserId, String toUserId);
}
