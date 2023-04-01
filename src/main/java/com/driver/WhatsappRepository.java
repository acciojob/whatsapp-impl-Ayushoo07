package com.driver;

import java.text.SimpleDateFormat;
import java.time.LocalDate;import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashMap<String,User> userDb;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userDb = new HashMap<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public void createUser(String name, String mobile)throws Exception
    {
        if(userDb.containsKey(mobile))
        {
            throw new Exception("User already exists");
        }
        else
        {
            userDb.put(mobile,new User(name,mobile));
        }
    }

    public Group createGroup(List<User> users)
    {
        if(users.size()==2)
        {
            userDb.put(users.get(0).getMobile(),users.get(0));
            userDb.put(users.get(1).getMobile(),users.get(1));
            Group group=new Group(users.get(1).getName(),2);
            return group;
        }

            ++customGroupCount;
            String groupName="Group "+customGroupCount;
            Group group=new Group(groupName,users.size());
            for(int i = 0; i < users.size();i++)
            {
                if(i==0)
                {
                    userDb.put(users.get(i).getMobile(),users.get(i));
                    adminMap.put(group,users.get(i));
                }
                else
                {
                    userDb.put(users.get(i).getMobile(),users.get(i));
                }
            }

            groupUserMap.put(group,users);
            return group;
    }
    public int createMessage(String content)
    {
        ++messageId;



        Message message=new Message(messageId,content);

        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group)throws Exception
    {
        if(!groupUserMap.containsKey(group))
        {
            throw new Exception("Group does not exist");
        }
        List<User> members=groupUserMap.get(group);

        if(!members.contains(sender))
        {
            throw new Exception("You are not allowed to send message");
        }

        if(groupMessageMap.containsKey(group))
        {
            List<Message> messages= groupMessageMap.get(group);
            messages.add(message);
            groupMessageMap.put(group, messages);
        }
        else
        {
            List<Message> messages= new ArrayList<>();
            messages.add(message);
            groupMessageMap.put(group, messages);
        }

        senderMap.put(message,sender);

        return groupMessageMap.get(group).size();
    }

    public void changeAdmin(User approver, User user, Group group)throws Exception
    {
        if(!groupUserMap.containsKey(group))
        {
            throw new Exception("Group does not exist");
        }

        if(adminMap.get(group)!=approver)
        {
            throw new Exception("Approver does not have rights");
        }

        List<User> members = groupUserMap.get(group);

        if(!members.contains(user))
        {
            throw new Exception("User is not a participant");
        }

        adminMap.put(group, user);

    }

    public int removeUser(User user)throws Exception
    {
        for(Map.Entry<Group,List<User>> hash : groupUserMap.entrySet())
        {
            if(hash.getValue().contains(user))
            {
                if(adminMap.get(hash.getKey())==user)
                {
                    throw new Exception("Cannot remove admin");
                }
                else
                {
                    List<User> members=hash.getValue();
                    members.remove(user);
                    groupUserMap.put(hash.getKey(),members);
                    userDb.remove(user.getMobile());

                    List<Message> messages=new ArrayList<>();
                    for (Map.Entry<Message,User> map:senderMap.entrySet())
                    {
                        if(map.getValue()==user)
                        {
                            messages.add(map.getKey());
                        }
                    }

                    List<Message> groupMessages=groupMessageMap.get(hash.getKey());

                    for (Message m : messages)
                    {
                        if(groupMessages.contains(m))
                            groupMessages.remove(m);
                        senderMap.remove(m);
                    }

                    groupMessageMap.put(hash.getKey(),groupMessages);

                    return hash.getValue().size() + senderMap.size()+ groupMessageMap.get(hash.getKey()).size();
                }
            }
        }

        throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int k)throws Exception
    {
        String ans="";

        int cnt=0;

        for(Map.Entry<Message,User> hash: senderMap.entrySet())
        {
            Date msgTime=hash.getKey().getTimestamp();

            if (start.after(msgTime) && end.before(msgTime))
            {
                cnt++;
                ans=ans+hash.getKey().getContent()+"\n";
            }
        }

        if(cnt<k)
            throw new Exception("K is greater than the number of messages");

        return ans;
    }
}
