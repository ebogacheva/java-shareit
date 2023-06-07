//package ru.practicum.shareit.item.dto;
//
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonDeserializer;
//import com.fasterxml.jackson.databind.JsonNode;
//
//import java.io.IOException;
//
//public class ItemDtoDeserializer extends JsonDeserializer<ItemDto> {
//
//    @Override
//    public ItemDto deserialize(JsonParser jp, DeserializationContext ctxt)
//            throws IOException {
//        JsonNode node = jp.getCodec().readTree(jp);
//        String name = null;
//        if (node.hasNonNull("name")) {
//           name = node.get("name").asText();
//        }
//        String description = null;
//        if (node.hasNonNull("description")) {
//            description = node.get("description").asText();
//        }
//        String available = null;
//        if (node.hasNonNull("available")) {
//            available = String.valueOf(node.get("available").asBoolean());
//        }
//        return new ItemDto(name, description, available);
//    }
//}
