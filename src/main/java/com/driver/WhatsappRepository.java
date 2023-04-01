package com.driver;

import java.text.SimpleDateFormat;
import java.time.LocalDate;import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below-mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }
    public String createUser(String name,String mobile) throws Exception{
        if(userMobile.contains(mobile))
            throw new Exception("User already exists");
        else {
            User user = new User(name,mobile);
            userMobile.add(mobile);
            return "SUCCESS";
        }
    }
    public Group createGroup(List<User> users){
        Group group = null;
        //if there are more than two users then it's a Group.
        if(users.size() > 2){
            this.customGroupCount++;
            group = new Group("Group "+this.customGroupCount,users.size());
        }else {//if there are only Two users then it's a Personal Chat and name of group in name of 2nd user.
            group = new Group(users.get(1).getName(),2);
        }
        groupUserMap.put(group,users);
        adminMap.put(group,users.get(0));
        return group;
    }
    public int createMessage(String content){
        this.messageId++;

        Message m = new Message(this.messageId,content);
        return this.messageId;
    }
    public int sendMessage(Message message,User sender,Group group) throws Exception {
        if(!groupUserMap.containsKey(group))
            throw new Exception("Group does not exist");

        if(!isValidUser(group,sender))
            throw new Exception("You are not allowed to send message");

        senderMap.put(message,sender);
        List<Message> messageList = groupMessageMap.get(group);
        if(messageList == null){
            messageList = new ArrayList<>();
        }
        messageList.add(message);
        groupMessageMap.put(group,messageList);
        return messageList.size();
    }
    //validate a sender if it is present in that group or not
    public boolean isValidUser(Group group,User sender){
        for(User user : groupUserMap.get(group)){
            if(user == sender)
                return true;
        }
        return false;
    }
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if(!groupUserMap.containsKey(group))
            throw new Exception("Group does not exist");

        if(adminMap.get(group) != approver)
            throw new Exception("Approver does not have rights");

        if(!isValidUser(group,user))
            throw new Exception("User is not a participant");

        //if all pre-requisite approved then change the new user to current admin for that group
        adminMap.put(group,user);
        //now we also update the list of USer for that group as only first member of group can be the admin
        List<User> users = groupUserMap.get(group);
        users.remove(0);
        users.add(0,user);
        users.add(approver);
        //now update the group user MAP also
        groupUserMap.put(group,users);

        return "SUCCESS";
    }
    public int removeUser(User user) throws Exception {
        Group group = null;
        // find in which group this user exists,if not exists then return exception
        for(Group grp : groupUserMap.keySet()){

            //check if current group has only 2 members then its a personal Chat
            if(groupUserMap.get(grp).size() < 3) continue;

            //check if the user present in that group or Not
            if(isValidUser(grp,user)){
                group = grp;
                break;
            }
        }
        // if user is not present in any of the group
        if(group == null)
            throw new Exception("User not found");

        //we have found user in Group,now validate if it is Admin or not
        if(adminMap.get(group) == user)
            throw new Exception("Cannot remove admin");

        //now we have a user which belong to a group and also not an admin,now remove it from all database

        //remove all messages from this user
        List<Message> messageList =  groupMessageMap.get(group);

        for(Map.Entry<Message,User> map : senderMap.entrySet()){
            Message message = map.getKey();
            User user1 = map.getValue();
            if(user1 == user){
                messageList.remove(message);
                senderMap.remove(message);
            }
        }
        //now updated message List for group where this user belong
        groupMessageMap.put(group,messageList);

        //now delete this user from group user map
        List<User> userList = groupUserMap.get(group);
        userList.remove(user);
        groupUserMap.put(group,userList);

        //also update the paricipant in this group
        int totalParticipants = group.getNumberOfParticipants();
        group.setNumberOfParticipants(totalParticipants-1);

        //now delete the mobile number from database for this user
        String mobno = user.getMobile();
        userMobile.remove(mobno);

        // user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        return group.getNumberOfParticipants() + groupMessageMap.get(group).size() + senderMap.size();
    }
    public String findMessage(Date start, Date end, int K) throws Exception{
        int countMessages = 0;
        for(Message message : senderMap.keySet()){
            if(message.getTimestamp().compareTo(start) > 0 && message.getTimestamp().compareTo(end) < 0)
                countMessages++;
        }
        //If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        if(countMessages < K)
            throw new Exception("K is greater than the number of messages");

        return "SUCCESS";
    }
    public HashMap<Group, List<User>> getGroupUserMap() {
        return groupUserMap;
    }

    public HashMap<Group, List<Message>> getGroupMessageMap() {
        return groupMessageMap;
    }

    public HashMap<Message, User> getSenderMap() {
        return senderMap;
    }

    public HashMap<Group, User> getAdminMap() {
        return adminMap;
    }

    public HashSet<String> getUserMobile() {
        return userMobile;
    }

    public int getCustomGroupCount() {
        return customGroupCount;
    }

    public int getMessageId() {
        return messageId;
    }
}