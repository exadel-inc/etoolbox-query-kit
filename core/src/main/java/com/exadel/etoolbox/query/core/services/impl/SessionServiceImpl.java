package com.exadel.etoolbox.query.core.services.impl;

import com.exadel.etoolbox.query.core.services.SessionService;
import org.osgi.service.component.annotations.Component;

import javax.jcr.Session;

@Component(service = SessionService.class)
public class SessionServiceImpl implements SessionService {

    @Override
    public void closeSession(Session session) {
        if (session != null && session.isLive()) {
            session.logout();
        }
    }
}
