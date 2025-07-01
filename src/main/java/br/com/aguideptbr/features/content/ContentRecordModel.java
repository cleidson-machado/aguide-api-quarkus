package br.com.aguideptbr.features.content;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "content_record")
public class ContentRecordModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    @NotBlank
    public String title;

    @Column(length = 1000)
    @Size(max = 1000)
    public String description;

    @Column(nullable = false, unique = true)
    public String url;

    @Column(name = "channel_name")
    public String channelName;

    @Column
    public String type; // VIDEO, ARTICLE, PODCAST, etc.

    @Column(name = "thumbnail_url")
    public String thumbnailUrl;

    public static ContentRecordModel findByTitle(String title_txt) {
        return find("title", title_txt).firstResult();
    }

}
