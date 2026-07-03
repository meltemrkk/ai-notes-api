package ai_notes_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // Sınırı tamamen kalktı, artık PDF metni bile alabilir
    @Column(columnDefinition = "TEXT")
    private String content;

    // Özet kısmı da uzun olabilir - sınırsız suan
    @Column(columnDefinition = "TEXT")
    private String summary;
}