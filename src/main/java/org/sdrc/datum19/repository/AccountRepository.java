package org.sdrc.datum19.repository;

import org.sdrc.datum19.usermgmt.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String> {

}
