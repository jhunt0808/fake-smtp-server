package de.gessnerfl.fakesmtp.server.impl;

import de.gessnerfl.fakesmtp.repository.EmailRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.subethamail.smtp.helper.SimpleMessageListener;

import java.io.IOException;
import java.io.InputStream;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = IOException.class)
public class EmailPersister implements SimpleMessageListener {
    private final EmailFactory emailFactory;
    private final EmailFilter emailFilter;
    private final EmailRepository emailRepository;
    private final Logger logger;

    @Autowired
    public EmailPersister(EmailFactory emailFactory, EmailFilter emailFilter, EmailRepository emailRepository, Logger logger) {
        this.emailFactory = emailFactory;
        this.emailFilter = emailFilter;
        this.emailRepository = emailRepository;
        this.logger = logger;
    }

    @Override
    public boolean accept(String from, String recipient) {
        return true;
    }

    @Override
    public void deliver(String sender, String recipient, InputStream data) throws IOException {
        logger.info("Received email from {} for {}", sender, recipient);

        var rawData = new RawData(sender, recipient, IOUtils.toByteArray(data));

        if(!emailFilter.ignore(sender,recipient)) {
            var email = emailFactory.convert(rawData);
            emailRepository.save(email);
        }
    }
}
