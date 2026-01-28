package com.aioversms.queryservice.sms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aioversms.queryservice.sms.entity.SmsMessage;

@Repository
public interface SmsMessageRepository extends JpaRepository<SmsMessage, Long> {
}