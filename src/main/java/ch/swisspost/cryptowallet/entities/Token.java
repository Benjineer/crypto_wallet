package ch.swisspost.cryptowallet.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Token")
public class Token {

  @Id
  private String jti;

  private String username;

  @Builder.Default
  private boolean revoked = false;

  @TimeToLive(unit = TimeUnit.MINUTES)
  private Integer ttlMinutes;
}
