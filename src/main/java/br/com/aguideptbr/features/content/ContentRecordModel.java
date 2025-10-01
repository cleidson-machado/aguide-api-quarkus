package br.com.aguideptbr.features.content;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Represents a content record (e.g., video, article, podcast) in the database.
 * This class utilizes the Active Record pattern provided by Panache.
 */
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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public ContentType type;

    @Column(name = "thumbnail_url")
    public String thumbnailUrl;

    /**
     * Searches for content records whose title starts with the given search term.
     * This search is case-insensitive and returns a list of results.
     * It is ideal for implementing autocomplete or dynamic search features.
     *
     * @param searchTerm The partial term to search for at the beginning of the title.
     * @return A list of ContentRecordModel matching the criteria. The list will be empty if no matches are found.
     */
    public static List<ContentRecordModel> searchByTitle(String searchTerm) {
        return list("lower(title) like ?1", searchTerm.toLowerCase() + "%");
    }

    /**
     * Finds a single content record by an exact title match.
     * This method is case-sensitive by default and expects the title to be unique.
     *
     * @param title_txt The exact title to search for.
     * @return The found ContentRecordModel object, or {@code null} if no content matches the exact title.
     * @see br.com.aguideptbr.features.content.ContentRecordResource#findByTitle(String)
     */
    public static ContentRecordModel findByTitle(String title_txt) {
        return find("title", title_txt).firstResult();
    }

}