-- =============================================
-- ⚠️ ARQUIVO OBSOLETO - NÃO MAIS UTILIZADO
-- =============================================
--
-- Este arquivo (import.sql) foi usado no início do projeto para popular
-- o banco de dados com dados de teste/desenvolvimento.
--
-- ❌ PROBLEMA: O Hibernate executava este arquivo SEMPRE que a aplicação
--              iniciava com quarkus.hibernate-orm.sql-load-script=import.sql
--
-- ✅ SOLUÇÃO ATUAL: Agora usamos FLYWAY para gerenciar o schema e dados:
--    - Migrations incrementais e versionadas (V1.0.0, V1.0.1, etc)
--    - Controle fino sobre quando e como os dados são inseridos
--    - Diferentes behaviors por ambiente (dev, prod)
--
-- 📁 Localização das Migrations:
--    - src/main/resources/db/migration/*.sql (PostgreSQL)
--    - src/test/resources/db/migration/h2/*.sql (H2 - testes)
--
-- 🔒 SEGURANÇA EM PRODUÇÃO:
--    - application-prod.properties: quarkus.hibernate-orm.database.generation=none
--    - application-prod.properties: quarkus.flyway.clean-at-start=false
--    - Apenas migrations incrementais são aplicadas
--
-- 👤 USUÁRIO PADRÃO:
--    - Email: contato@aguide.space
--    - Nome: protouser
--    - Senha: (hash BCrypt via migration V1.0.6)
--    - Role: ADMIN
--
-- 📝 Data de descontinuação: 2026-02-04
-- 💡 Este arquivo está sendo mantido apenas para referência histórica.
--    Pode ser removido em futuras versões após confirmação.
--
-- =============================================

-- CONTEÚDO ORIGINAL (NÃO EXECUTADO - TODOS COMENTADOS):
-- import.sql com UUIDs gerados automaticamente pelo PostgreSQL
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'João', 'Silva', 'joao@example.com', 'senha123');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Maria', 'Souza', 'maria@example.com', 'senha456');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Carlos', 'Almeida', 'carlos@example.com', 'senha789');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Ana', 'Lima', 'ana@example.com', 'senhaabc');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Bruno', 'Oliveira', 'bruno@example.com', 'senhaxyz');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Fernanda', 'Costa', 'fernanda.costa@example.com', 'segredo1');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Rafael', 'Martins', 'rafael.martins@example.com', '123rafael');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Juliana', 'Mendes', 'juliana.mendes@example.com', 'ju456senha');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Gustavo', 'Dias', 'gustavo.dias@example.com', 'gustavopw');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Paula', 'Ferreira', 'paula.ferreira@example.com', 'p@ulinha');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Thiago', 'Rocha', 'thiago.rocha@example.com', 'senha789!');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Camila', 'Barros', 'camila.barros@example.com', 'camilinha');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Ricardo', 'Teixeira', 'ricardo.teixeira@example.com', 'ric123');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Larissa', 'Ramos', 'larissa.ramos@example.com', 'lar1ssa');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'André', 'Pereira', 'andre.pereira@example.com', 'andrepass');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Letícia', 'Azevedo', 'leticia.azevedo@example.com', 'lety@123');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Eduardo', 'Moreira', 'eduardo.moreira@example.com', 'edpass22');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Vanessa', 'Carvalho', 'vanessa.carvalho@example.com', 'vcarv123');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Felipe', 'Nogueira', 'felipe.nogueira@example.com', 'nog@456');
-- INSERT INTO app_user (id, name, surname, email, passwd) VALUES (gen_random_uuid(), 'Isabela', 'Santos', 'isabela.santos@example.com', 'isabelapw');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Understanding Java Streams', 'An introduction to Java Streams API.', 'https://example.com/java-streams', 'JavaZone', 'ARTICLE', 'https://img.example.com/thumbs/1.jpg', '27', 'Education', 'java, streams, programming, tutorial', NULL, NULL, NULL, NULL, 15420, 892, 45, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Spring Boot vs Quarkus', 'Comparing Spring Boot and Quarkus for modern apps.', 'https://example.com/quarkus-vs-spring', 'DevTalks', 'ARTICLE', 'https://img.example.com/thumbs/2.jpg', '28', 'Science & Technology', 'spring boot, quarkus, java, framework, comparison', NULL, NULL, NULL, NULL, 23150, 1340, 78, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'REST APIs with Quarkus', 'How to build REST services using Quarkus.', 'https://example.com/rest-quarkus', 'CodeCast', 'VIDEO', 'https://img.example.com/thumbs/3.jpg', '27', 'Education', 'quarkus, rest api, java, microservices, tutorial', 1245, 'PT20M45S', 'hd', true, 45780, 2890, 156, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'The Future of Java', 'Panel discussion about upcoming Java features.', 'https://example.com/future-java', 'TechToday', 'PODCAST', 'https://img.example.com/thumbs/4.jpg', '28', 'Science & Technology', 'java, future, technology, podcast, discussion', 3600, 'PT1H', NULL, NULL, 12340, 567, 89, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Microservices Design Patterns', 'Learn about common patterns in microservices.', 'https://example.com/microservices-patterns', 'CloudDaily', 'ARTICLE', 'https://img.example.com/thumbs/5.jpg', '27', 'Education', 'microservices, design patterns, architecture, cloud', NULL, NULL, NULL, NULL, 34560, 1890, 234, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Deploying with Docker', 'Step-by-step guide to containerizing your app.', 'https://example.com/docker-deploy', 'DevOpsBase', 'VIDEO', 'https://img.example.com/thumbs/6.jpg', '28', 'Science & Technology', 'docker, devops, containers, deployment, tutorial', 892, 'PT14M52S', 'hd', true, 67890, 4230, 312, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'JPA Tips & Tricks', 'Best practices for using JPA effectively.', 'https://example.com/jpa-tips', 'DataNinja', 'ARTICLE', 'https://img.example.com/thumbs/7.jpg', '27', 'Education', 'jpa, hibernate, database, java, best practices', NULL, NULL, NULL, NULL, 18920, 1120, 67, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Intro to Reactive Programming', 'Why reactive programming matters today.', 'https://example.com/reactive-intro', 'StreamFlow', 'VIDEO', 'https://img.example.com/thumbs/8.jpg', '28', 'Science & Technology', 'reactive, programming, async, java, tutorial', 1567, 'PT26M7S', 'hd', false, 52340, 3450, 189, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Kubernetes for Beginners', 'Getting started with Kubernetes and pods.', 'https://example.com/k8s-start', 'CloudToday', 'VIDEO', 'https://img.example.com/thumbs/9.jpg', '27', 'Education', 'kubernetes, k8s, devops, containers, beginner', 2134, 'PT35M34S', 'hd', true, 98760, 6780, 445, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Exploring RESTEasy in Quarkus', 'Build powerful REST APIs with RESTEasy.', 'https://example.com/resteasy-quarkus', 'DevQuark', 'ARTICLE', 'https://img.example.com/thumbs/10.jpg', '28', 'Science & Technology', 'resteasy, quarkus, rest api, java', NULL, NULL, NULL, NULL, 21450, 1230, 92, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Security in Java EE', 'How to secure Java enterprise applications.', 'https://example.com/javaee-security', 'SecureCode', 'ARTICLE', 'https://img.example.com/thumbs/11.jpg', '27', 'Education', 'security, java ee, enterprise, authentication, authorization', NULL, NULL, NULL, NULL, 28340, 1670, 134, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Performance Tuning in Java', 'Optimize your Java app for max performance.', 'https://example.com/java-tuning', 'PerfLab', 'ARTICLE', 'https://img.example.com/thumbs/12.jpg', '28', 'Science & Technology', 'performance, optimization, java, jvm, tuning', NULL, NULL, NULL, NULL, 41230, 2890, 201, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'RESTful Services in Practice', 'From theory to production.', 'https://example.com/rest-practice', 'CodeFlow', 'VIDEO', 'https://img.example.com/thumbs/13.jpg', '27', 'Education', 'rest, api, production, best practices, tutorial', 1789, 'PT29M49S', 'hd', true, 56780, 3890, 267, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Async Programming in Java', 'Writing non-blocking code with CompletableFuture.', 'https://example.com/async-java', 'AsyncTalks', 'ARTICLE', 'https://img.example.com/thumbs/14.jpg', '28', 'Science & Technology', 'async, java, completablefuture, concurrency, programming', NULL, NULL, NULL, NULL, 32450, 2120, 178, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Quarkus with Hibernate ORM', 'Using Hibernate effectively in Quarkus.', 'https://example.com/quarkus-hibernate', 'DataDriven', 'ARTICLE', 'https://img.example.com/thumbs/15.jpg', '27', 'Education', 'quarkus, hibernate, orm, database, java', NULL, NULL, NULL, NULL, 27890, 1560, 112, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Testing Quarkus Apps', 'Unit & integration testing in Quarkus.', 'https://example.com/quarkus-testing', 'TestLab', 'VIDEO', 'https://img.example.com/thumbs/16.jpg', '28', 'Science & Technology', 'testing, quarkus, unit test, integration test, java', 1423, 'PT23M43S', 'hd', true, 38920, 2340, 145, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Java Memory Management', 'Deep dive into JVM memory and GC.', 'https://example.com/java-memory', 'JVMInside', 'ARTICLE', 'https://img.example.com/thumbs/17.jpg', '27', 'Education', 'jvm, memory, garbage collection, performance, java', NULL, NULL, NULL, NULL, 49560, 3120, 289, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Kotlin for Java Devs', 'Why Java devs should look at Kotlin.', 'https://example.com/kotlin-java', 'JetBrainsZone', 'ARTICLE', 'https://img.example.com/thumbs/18.jpg', '28', 'Science & Technology', 'kotlin, java, programming, language, comparison', NULL, NULL, NULL, NULL, 36780, 2450, 198, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Podcast: Java in 2025', 'Industry leaders talk Java''s future.', 'https://example.com/java-2025-podcast', 'DevVoices', 'PODCAST', 'https://img.example.com/thumbs/19.jpg', '28', 'Science & Technology', 'java, podcast, future, technology, industry', 4200, 'PT1H10M', NULL, NULL, 18920, 890, 123, 'en', 'en');

-- INSERT INTO content_record (id, title, description, url, channel_name, content_type, thumbnail_url, category_id, category_name, tags, duration_seconds, duration_iso, definition, caption, view_count, like_count, comment_count, default_language, default_audio_language)
-- VALUES (gen_random_uuid(), 'Quarkus Boot Time Secrets', 'Make your app start in under 1s.', 'https://example.com/quarkus-boot', 'QuarkusHQ', 'VIDEO', 'https://img.example.com/thumbs/20.jpg', '27', 'Education', 'quarkus, performance, boot time, optimization, java', 678, 'PT11M18S', 'hd', false, 72340, 5120, 378, 'en', 'en');
