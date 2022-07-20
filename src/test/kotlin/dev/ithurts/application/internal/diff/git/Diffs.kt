package dev.ithurts.application.internal.diff.git

import io.reflectoring.diffparser.api.UnifiedDiffParser
import io.reflectoring.diffparser.api.model.Diff

object Diffs {
    val ONE_LINE_ADDED:  MutableList<Diff> = UnifiedDiffParser().parse(
        """
            Index: src/main/java/ru/verkhovin/poker/controller/RoomResource.java
            IDEA additional info:
            Subsystem: com.intellij.openapi.diff.impl.patch.CharsetEP
            <+>UTF-8
            ===================================================================
            diff --git a/src/main/java/ru/verkhovin/poker/controller/RoomResource.java b/src/main/java/ru/verkhovin/poker/controller/RoomResource.java
            --- a/src/main/java/ru/verkhovin/poker/controller/RoomResource.java	(revision 1e1cf0a3f736b3eaca87c0333f9c2ac51c1a663a)
            +++ b/src/main/java/ru/verkhovin/poker/controller/RoomResource.java	(revision 0680b74d1d15731d5ad35f1c267d743b8b07ca06)
            @@ -23,6 +23,7 @@
               @PostMapping
               public ResponseEntity<Void> createRoom() {
                 Long roomId = roomService.createRoom();
            +    //change one more change
                 return ResponseEntity.created(URI.create("/api/rooms/" + roomId)).build();
               }
             

        """.trimIndent().toByteArray()
    )


    val TWO_LINES_ADDED: MutableList<Diff> = UnifiedDiffParser().parse(
        """
            Index: src/main/java/ru/verkhovin/poker/controller/RoomResource.java
            IDEA additional info:
            Subsystem: com.intellij.openapi.diff.impl.patch.CharsetEP
            <+>UTF-8
            ===================================================================
            diff --git a/src/main/java/ru/verkhovin/poker/controller/RoomResource.java b/src/main/java/ru/verkhovin/poker/controller/RoomResource.java
            --- a/src/main/java/ru/verkhovin/poker/controller/RoomResource.java	(revision 1e1cf0a3f736b3eaca87c0333f9c2ac51c1a663a)
            +++ b/src/main/java/ru/verkhovin/poker/controller/RoomResource.java	(date 1658248920782)
            @@ -23,11 +23,13 @@
               @PostMapping
               public ResponseEntity<Void> createRoom() {
                 Long roomId = roomService.createRoom();
            +    //change one more change
                 return ResponseEntity.created(URI.create("/api/rooms/" + roomId)).build();
               }
             
               @GetMapping("/{id}")
               public RoomDto getRoom(@PathVariable("id") Long id){
            +    // another change
                 return roomService.getRoom(id);
               }
             }

        """.trimIndent().toByteArray()
    )
}