package ua.fpv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "oauth2_registered_client")
public class OAuthClient {

    @Id
    private String id;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_name")
    private String clientName;

}
