package com.exadel.etoolbox.query.core.services;

import javax.jcr.Session;

public interface SessionService {
    void closeSession(Session session);
}
