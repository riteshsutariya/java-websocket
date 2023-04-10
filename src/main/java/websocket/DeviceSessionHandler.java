/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package websocket;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;
import model.Device;

/**
 *
 * @author ritesh
 */
@ApplicationScoped
public class DeviceSessionHandler {
    
    private int deviceId = 0;
    private final Set<Session> sessions = new HashSet<>();
    private final Set<Device> devices = new HashSet<>();
    
    public void addSession(Session session) {
        sessions.add(session);
        for (Device device : devices) {
            JsonObject addMessage = createAddMessage(device);
            sendToSession(session, addMessage);
        }
    }
    
    public void removeSession(Session session) {
        sessions.remove(session);
    }
    
    public void addDevice(Device device) {
        device.setId(deviceId);
        devices.add(device);
        deviceId++;
        JsonObject addMessage = createAddMessage(device);
        sendToAllConnectedSessions(addMessage);
    }
    
    public void removeDevice(int id) {
//        devices.remove(device);
        Device device = getDeviceById(id);
        if (device != null) {
            devices.remove(device);
            JsonProvider provider = JsonProvider.provider();
            JsonObject removeMessage = provider.createObjectBuilder().add("action", "remove").add("id", id).build();
            sendToAllConnectedSessions(removeMessage);
        }
    }
    
    public void toggleDevice(int id) {
        Device device = getDeviceById(id);
        JsonProvider provider = JsonProvider.provider();
        System.err.println("toggle request");
        if (device != null) {
            if ("On".equals(device.getStatus())) {
                device.setStatus("Off");
            } else {
                device.setStatus("On");
            }
            JsonObject updateDeviceMessage = provider.createObjectBuilder().add("action", "toggle").add("id", id).add("status", device.getStatus()).build();
            sendToAllConnectedSessions(updateDeviceMessage);
        }
    }
    
    private Device getDeviceById(int id) {
        for (Device device : devices) {
            if (device.getId() == id) {
                return device;
            }
        }
        return null;
    }
    
    private JsonObject createAddMessage(Device device) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject addMessage = provider.createObjectBuilder().add("action", "add").add("id", device.getId()).add("name", device.getName()).add("type", device.getType()).add("status", device.getStatus()).add("description", device.getDescription()).build();
        return addMessage;
    }
    
    private void sendToAllConnectedSessions(JsonObject message) {
        for (Session session : sessions) {
            sendToSession(session, message);
        }
    }
    
    private void sendToSession(Session session, JsonObject message) {
        try{
            session.getBasicRemote().sendText(message.toString());
        }catch(Exception ex)
        {
            sessions.remove(session);
            Logger.getLogger(DeviceSessionHandler.class.getName()).log(Level.SEVERE,null,ex);
        }
    }
}
