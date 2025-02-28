package com.bohdanzhuvak.cartservice.repository;

import com.bohdanzhuvak.cartservice.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {

  Optional<Cart> findByUserId(String userId);
}