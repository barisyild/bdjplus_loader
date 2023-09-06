package com.bdjplus.injector;

import com.sony.bdjstack.system.BDJModule;
import org.dvb.event.EventManager;
import org.dvb.event.UserEvent;
import org.dvb.event.UserEventRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InjectorXlet extends java.awt.Container implements javax.tv.xlet.Xlet, org.dvb.event.UserEventListener {
    private org.havi.ui.HScene scene;
    private java.awt.Font font;
    private int currentIndex = 0;
    private final String rootDirectory = "/app0/cdc/modules/jar";
    private String currentDirectory = "/";
    private List files = new ArrayList();
    public void initXlet(javax.tv.xlet.XletContext context) {
        setSize(1920, 1080);
        scene = org.havi.ui.HSceneFactory.getInstance().getDefaultHScene();
        scene.add(this);
        scene.validate();
    }

    public void startXlet() {
        setVisible(true);
        scene.setVisible(true);
        setDirectory("/");
        repaint();

        UserEventRepository userEventRepo = new UserEventRepository("input");
        userEventRepo.addKey(38);
        userEventRepo.addKey(40);
        userEventRepo.addKey(10);
        EventManager.getInstance().addUserEventListener(this, userEventRepo);
    }

    public void pauseXlet() {
        setVisible(false);
        BDJModule.log("pauseXlet");
    }

    public void destroyXlet(boolean unconditional) {
        BDJModule.log("destroyXlet");

        EventManager.getInstance().removeUserEventListener(this);

        getGraphics().clearRect(0, 0, getWidth(), getHeight());
        //repaint();

        scene.remove(this);
        scene = null;
    }

    public void setDirectory(String directory)
    {
        BDJModule.log("Change Directory: " + rootDirectory + directory);
        File targetDirectory = new File(rootDirectory + directory);
        if(!targetDirectory.getAbsolutePath().startsWith(rootDirectory))
            return;

        try {
            files.clear();
            File[] fileList = targetDirectory.listFiles();
            if(fileList == null)
                throw new Exception();

            if(!directory.equals("/"))
                files.add(null);

            for(int i = 0; i < fileList.length; i++)
            {
                File file = fileList[i];

                if(file.isDirectory() || file.getName().endsWith(".jar"))
                {
                    files.add(file);
                }
            }

            currentIndex = 0;
            currentDirectory = directory;
            repaint();
        }
        catch (Exception e)
        {
            BDJModule.log(e);
            if(!directory.equals("/"))
                setDirectory("/");
        }
    }

    public void paint(java.awt.Graphics g) {
        if(this.scene == null)
            return;

        BDJModule.log("paint");

        g.setColor(new java.awt.Color(0x000000));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new java.awt.Color(0xffffff));
        if (font == null) {
            font = new java.awt.Font(null, java.awt.Font.PLAIN, 30);
            g.setFont(font);
        }

        if(files.size() > 0)
            g.drawString(">", 50, (currentIndex + 1) * 50);

        for(int i = 0; i < files.size(); i++)
        {
            int drawPos = i + 1;
            File file = (File) files.get(i);
            if(file == null)
            {
                g.drawString("..", 70, drawPos * 50);
                continue;
            }
            String ext = file.isFile() ? "F" : "D";
            g.drawString(ext + "| " +  file.getName(), 70, drawPos * 50);
        }
    }

    public void userEventReceived(UserEvent userEvent) {
        // 38 - up
        // 40 - down
        // 10 - X
        if(userEvent.getType() != 401)
        {
            return;
        }

        if(userEvent.getCode() == 38)
        {
            if(currentIndex > 0)
            {
                currentIndex--;
                repaint();
            }
        }else if(userEvent.getCode() == 40) {
            if (currentIndex < files.size() - 1)
            {
                currentIndex++;
                repaint();
            }
        }else if(userEvent.getCode() == 10)
        {
            File file = (File) files.get(currentIndex);
            if(file == null)
            {
                // Back
                String newDirectory = currentDirectory.substring(0, currentDirectory.lastIndexOf("/"));
                setDirectory(newDirectory);
            }else if(file.isFile())
            {
                destroyXlet(true);

                try {
                    String filePath = file.getAbsolutePath().substring(0, file.getPath().lastIndexOf("/"));
                    if(!filePath.endsWith("/"))
                        filePath += "/";

                    BDJModule.debug("Load: " + filePath);

                    Thread.currentThread().setPriority(10);

                    System.setProperty("bluray.vfs.root", filePath);
                    BDJModule.loadJar(file.getAbsolutePath());
                }
                catch (Exception e)
                {
                    BDJModule.log(e);
                }
            }else if(file.isDirectory())
            {
                setDirectory(currentDirectory + "/" + file.getName());
            }

            BDJModule.log("Selected: " + file);
        }
    }
}