package ai_notes_api.service;

import ai_notes_api.entity.Note;
import ai_notes_api.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final AiService aiService;

    // Her iki servisi de (Veritabanı ve Yapay Zeka) içeri alion
    public NoteService(NoteRepository noteRepository, AiService aiService) {
        this.noteRepository = noteRepository;
        this.aiService = aiService;
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Note saveNote(Note note) {
        // 1. Dışarıdan gelen uzun notu AiService'e gönderip özetini alion
        String generatedSummary = aiService.getSummaryFromAi(note.getContent());

        // 2. Gelen özeti notumuzun "summary" kısmına ekleme
        note.setSummary(generatedSummary);

        // 3. Artık özeti de olan notu veritabanına kaydediyo
        return noteRepository.save(note);
    }

    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }
}