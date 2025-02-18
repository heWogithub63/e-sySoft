package easysoft.freebrowser;


import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.File;
import java.util.Arrays;

import static easysoft.freebrowser.FileBrowser.*;
import static easysoft.freebrowser.TextEditorFragment.TxEditor;
import static easysoft.freebrowser.emailDisplayFragment.mailTx;

public class showMessageFragment extends Fragment {

    public View view;
    FrameLayout messageLayout;
    LinearLayout mainLinLy;
    RelativeLayout editRel;
    public TextView[] messageTx;
    public String kindOf = "";
    public String[] messageString = new String[0],
            steerPanel = new String[0];
    public EditText requestedText;
    public boolean  progressBar;
    public int messageTimer;
    int txColor, backgrColor;
    static int selectStartPos, selectStopPos, txLengthDiff;
    public static showMessageFragment newInstance() {
        return new showMessageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kindOf = getArguments().getString("KINDOF");
            messageString = getArguments().getStringArray("MESSAGE_STRING");
            messageTimer = getArguments().getInt("MESSAGE_TIMER");
            progressBar = getArguments().getBoolean("PROGRESS_BAR");

            steerPanel = new String[]{"CANCEL", "OK"};
            if(kindOf.equals("Extern_Device_Permission") || kindOf.equals("Instruction_Manuel"))
                steerPanel = new String[]{"OK"};
            if(kindOf.equals("FindResult"))
                steerPanel = new String[]{"CANCEL"};
            if(kindOf.equals("httpsRequest"))
                steerPanel = new String[0];
        }
        txColor = getResources().getColor(R.color.black);
        backgrColor = getResources().getColor(R.color.white);

        messageLayout = fileBrowser.frameLy.get(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_message, container, false);
        mainLinLy = (LinearLayout) view.findViewById(R.id.mainLin);
        mainLinLy.setBackgroundColor(backgrColor);
        mainLinLy.setPadding(5,5,5,5);
        createMessagePanel(mainLinLy);

        messageLayout.bringToFront();
        return view;
    }

    public void createMessagePanel(LinearLayout lin) {

        RelativeLayout.LayoutParams txRelParam = new RelativeLayout.LayoutParams(2*displayWidth/5, displayHeight/5);

        LinearLayout mTxLin = new LinearLayout(fileBrowser);
        if(kindOf.equals("Instruction_Manuel"))
            txRelParam = new RelativeLayout.LayoutParams(displayWidth/2, 3*displayHeight/5);
        mTxLin.setLayoutParams(txRelParam);
        mTxLin.setPadding(5,5,5,5);
        mTxLin.setOrientation(LinearLayout.VERTICAL);

        int scFact = 4;
        if(messageTimer != 0)
            scFact = 6;
        else if(messageTimer == 0 && messageString.length <4)
            scFact = 3;
        ScrollView mTxScr = new ScrollView(fileBrowser);
        mTxScr.setBackgroundColor(getResources().getColor(R.color.white_overlay));
        mTxScr.setLayoutParams(new RelativeLayout.LayoutParams(txRelParam));
        mTxScr.setPadding(5,5,5,5);

        messageTx = new TextView[0];

        for(int i=0;i<messageString.length;i++) {
            messageTx = Arrays.copyOf(messageTx, messageTx.length +1);
            messageTx[messageTx.length -1] = new TextView(fileBrowser);
            messageTx[messageTx.length -1].setTextColor(txColor);
            messageTx[messageTx.length -1].setTextSize((float) (textSize));
            messageTx[messageTx.length -1].setTag(messageString[i]);
            messageTx[messageTx.length -1].setText(messageString[i]);
            if(kindOf.startsWith("Find")) {
                if(messageString[i].contains("/"))
                    messageTx[messageTx.length -1].setText(messageString[i].substring(messageString[i].lastIndexOf("/") +1));
                messageTx[messageTx.length - 1].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (TextView mTx : messageTx)
                            mTx.setTextColor(getResources().getColor(R.color.black));
                        ((TextView) v).setTextColor(getResources().getColor(R.color.blue));

                        String url = v.getTag().toString().replace(":","/"),
                                urldevicePath = urldevice;
                        devicePath = url;
                        if(!url.equals(urldevice))
                            urldevicePath = url.substring(urldevice.length() + 1);

                        paramList = fileBrowser.createArrayList(urldevicePath);

                        if(urldevicePath.length() != 0 || urldevicePath.contains("/")) {
                            fileBrowser.createFolder(fileBrowser.urldevice);
                            fileBrowser.fragmentStart(fileBrowser.filebrowser_01, 1,"fileBrowser01", null, (int) (250 * xfact), (int) (440 * yfact),
                                    (int) (4 * displayWidth / 5 - 80 * yfact), (int) (5 * displayHeight / 7));
                        } else
                            fileBrowser.createFolder(fileBrowser.urldevice);
                    }
                });
            }

            mTxLin.addView(messageTx[messageTx.length - 1]);
        }
        mTxScr.addView(mTxLin);

        if(!kindOf.equals("httpsRequest")) {
            lin.addView(mTxScr);
        }

        if(kindOf.startsWith("ask") || kindOf.startsWith("create") || kindOf.startsWith("find") || kindOf.endsWith("Document_Save") || kindOf.equals("httpsRequest")) {

            editRel = new RelativeLayout(fileBrowser);
            editRel.setBackgroundColor(getResources().getColor(R.color.white_overlay));
            RelativeLayout.LayoutParams requestParam = new RelativeLayout.LayoutParams(displayWidth/3, displayHeight/18);
            requestParam.addRule(RelativeLayout.CENTER_IN_PARENT);
            editRel.setLayoutParams(requestParam);
            editRel.setPadding(15,5,5,5);
            String tx = "";
            if(devicePath != null && devicePath.length() > 0) {
                if (devicePath.substring(1).contains("."))
                    tx = devicePath.substring(devicePath.lastIndexOf("/")+1, devicePath.lastIndexOf("."));
                else if (kindOf.startsWith("create"))
                    tx = devicePath.substring(devicePath.lastIndexOf("/") + 1) + "/";
                else if (kindOf.startsWith("find"))
                    tx = "";
                else
                    tx = devicePath.substring(devicePath.lastIndexOf("/") + 1);

            } else if(!kindOf.equals("httpsRequest")){
                fileBrowser.messageStarter("Instruction_TxDocumentSave", docu_Loader("Language/" + language + "/Instruction_TxDokumentSave.txt"),  8000);
                return;
            }
            if(kindOf.equals("httpsRequest"))
                tx = "https://";


            requestedText = new EditText(fileBrowser);
            requestedText.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth/3, displayHeight/18));
            requestedText.setTextSize(textSize);
            requestedText.setTextColor(getResources().getColor(R.color.black));
            requestedText.setText(tx);
            if(fileBrowser.createTxEditor != null && fileBrowser.createTxEditor.isVisible())
                if(kindOf.endsWith("Document_Save") && kindOf.startsWith("Pdf") && fileBrowser.createTxEditor.loadedFile.endsWith(".pdf")) {
                    requestedText.setText(fileBrowser.createTxEditor.loadedFile.substring(fileBrowser.createTxEditor.loadedFile.lastIndexOf("/") +1,
                            fileBrowser.createTxEditor.loadedFile.lastIndexOf(".")));
                    requestedText.setEnabled(false);
                }

            requestedText.setBackgroundColor(getResources().getColor(R.color.grey_overlay));
            requestedText.setFocusable(true);
            requestedText.setPadding(10,0,10,0);
            requestedText.setShowSoftInputOnFocus(false);
            requestedText.requestFocus();
            fileBrowser.keyboardTrans = requestedText;
            if(kindOf.equals("pdfCombinedDocument_Save"))  {
                String[] folder = new File(devicePath).list();
                String pdfs = "PdfName";

                for(String s:folder)
                    if(s.endsWith(".pdf")) {
                        pdfs = pdfs +", "+ s;
                    }
                tx = pdfs;
                requestedText.setText(tx);
                requestedText.setLayoutParams(new RelativeLayout.LayoutParams(2*fileBrowser.frameLy.get(0).getWidth()/2, fileBrowser.frameLy.get(0).getHeight()/6));
                selectStartPos = 0;
                selectStopPos = 7;
                txLengthDiff = tx.length();
                requestedText.setSelection(selectStartPos,selectStopPos);
                createSelector();
                if(fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible()) {
                    int fact = displayHeight/18,
                            fact01 = displayHeight/18;
                    if(yfact < 0.625) {
                        fact = displayHeight / 28;
                        fact01 = 0;
                    }
                    fileBrowser.keyboardTrans = requestedText;
                    fileBrowser.fragmentStart(fileBrowser.softKeyBoard, 6,"softKeyBoard",null,5,(int)(2*displayHeight/3 -fact),
                                displayWidth -10, (int)(displayHeight/3) +fact01);
                }
            } else if(kindOf.equals("httpsRequest")) {
                RelativeLayout.LayoutParams requParam = new RelativeLayout.LayoutParams(7*displayWidth/9, displayHeight / 18);
                requParam.addRule(RelativeLayout.CENTER_IN_PARENT);
                requestedText.setLayoutParams(requParam);
                editRel.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                requestedText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if(!b && fileBrowser.softKeyBoard != null && fileBrowser.softKeyBoard.isVisible()) {
                            fileBrowser.changeIcon(fileBrowser.webBrowserDisplay.steerImgs[2], "browserIcons", "open", "closed");
                            fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);
                            fileBrowser.fragmentShutdown(fileBrowser.showMessage,0);
                        }
                    }
                });
            }
            requestedText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int fact = displayHeight/18,
                            fact01 = displayHeight/18;

                    if(yfact < 0.625) {
                        fact = displayHeight / 28;
                        fact01 = 0;
                    }
                    if(yfact >= 0.8) {
                        fact01 = displayHeight/12;
                    }
                    fileBrowser.keyboardTrans = ((EditText) v);
                    if(fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible())
                       fileBrowser.fragmentStart(fileBrowser.softKeyBoard, 6,"softKeyBoard",null,5,(int)(2*displayHeight/3 -fact),
                               displayWidth -10, (int)(displayHeight/3 +fact01));
                }
            });
            editRel.addView(requestedText);
            lin.addView(editRel);
        }

        if(messageTimer == 0) {
            int ux = 85, uy = 25;
            if(yfact <= 0.6 && displayWidth < 500) {
                ux = 30;
                uy = 7;
            }

            TextView[] steerButton = new TextView[0];
            LinearLayout.LayoutParams steerParam = new LinearLayout.LayoutParams((3*displayWidth/5), displayHeight/12);
            LinearLayout RelLy = new LinearLayout(fileBrowser);
            RelLy.setLayoutParams(new LinearLayout.LayoutParams(steerParam));
            RelLy.setPadding((int)(ux*xfact),(int)(uy*xfact),0,0);

            RelativeLayout steerRel = new RelativeLayout(fileBrowser);
            steerRel.setLayoutParams(steerParam);
            if(kindOf.endsWith("Delete") || kindOf.equals("Instruction_Manuel") || kindOf.equals("PermissionDenied") || kindOf.equals("mailSendRequest")) {
                for (int i = 0; i < steerPanel.length; i++) {
                    steerButton = Arrays.copyOf(steerButton, steerButton.length + 1);
                    steerButton[steerButton.length - 1] = new TextView(fileBrowser);
                    steerButton[steerButton.length - 1].setTextColor(getResources().getColor(R.color.white));
                    steerButton[steerButton.length - 1].setText(steerPanel[i]);
                    steerButton[steerButton.length - 1].setTextSize((float) (textSize));
                    steerButton[steerButton.length - 1].setTag(kindOf + "  " + steerPanel[i]);
                    steerButton[steerButton.length - 1].setPadding(10, 0, 10, 0);
                    steerButton[steerButton.length - 1].setX((float) (i * displayWidth / 5));
                    steerButton[steerButton.length - 1].setBackgroundColor(getResources().getColor(R.color.black_overlay));
                    steerButton[steerButton.length - 1].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String kind_of = view.getTag().toString().substring(view.getTag().toString().lastIndexOf("  ") + 2);
                            if (kind_of.equals("OK")) {
                                clickOk();
                            } else if (kindOf.equals("InstallPermissionDenied"))
                                fileBrowser.createList_systemUrl(2, 4);

                            fileBrowser.threadStop = true;
                            fileBrowser.fragmentShutdown(fileBrowser.showMessage, 0);

                        }
                    });
                    steerRel.addView(steerButton[steerButton.length - 1]);
                }
            }
            if(kindOf.equals("Instruction_Manuel"))
               steerRel.addView(createCopyButton(messageTx.length -1));
            RelLy.addView(steerRel);
            lin.addView(RelLy);
        } else {
            new messageTimer().start();
        }
    }
    public void clickOk () {

        if (kindOf.contains("ask") ) {
            fileBrowser.timeImage.setVisibility(View.VISIBLE);
            fileBrowser.timerAnimation.start();
            String todo = commandString.substring(0, commandString.indexOf("  ")),
                    from = commandString.substring(commandString.indexOf("  ") + 2),
                    kind = "",
                    to = "",
                    newName = "";

            if(requestedText != null)
                newName = requestedText.getText().toString().replace(" ", "").trim();


            if(kindOf.endsWith("Rename")) {
                if(from.substring(from.lastIndexOf("/")+1).contains("."))
                    kind = from.substring(from.lastIndexOf("."));

                if(newName.contains("."))
                    newName = newName.substring(0, newName.lastIndexOf("."));
                newName = newName +kind;
                to = from.substring(0, from.lastIndexOf("/") +1) +newName;
                if(to.startsWith("'"))
                    to = to.substring(1);
                // new FileBrowser.dataHandler(todo, from, to,null).start();
            } else if(kindOf.endsWith("Search")) {
                to = " -> find " + newName;
            } else if (kindOf.endsWith("Delete")) {
                //new FileBrowser.dataHandler(todo, from, to,null).start();
                fileBrowser.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.timerAnimation.stop();
                if(kindOf.contains("Trash")) {
                    if (fileBrowser.showList != null && fileBrowser.showList.isVisible())
                        fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                    if (kindOf.equals("_askTrashDelete")) {
                        while(new File(fileBrowser.context.getFilesDir() + "/TrashIndex/").listFiles().length > 0)
                              fileBrowser.deleteDir_Files(fileBrowser.context.getFilesDir() + "/TrashIndex/");
                        fileBrowser.read_writeFileOnInternalStorage("write","pathCollection", "PathList.txt", "");
                        fileBrowser.changeIcon(headMenueIcon01[7], "sideLeftMenueIcons", "open", "closed");
                        headMenueIcon01[headMenueIcon01.length - 1].setEnabled(false);

                        return;
                    }
                }
            } else if (kindOf.endsWith("Create")) {

                String index = requestedText.getText().toString().replace(" ", "_").replace(".", "-");
                if (index.contains("/"))
                    index = index.substring(index.lastIndexOf("/") + 1);
                commandString = commandString + "/" + index;
                //if (kindOf.endsWith("Ordner"))
                    from = from + "/" + index;
                //new FileBrowser.dataHandler(todo, from, to,null).start();

            }
            final String com = todo, fromPath = from, toPath = to;
            fileBrowser.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fileBrowser.startTerminalCommands(com, fromPath, toPath);
                }
            });
            if(fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                fileBrowser.changeIcon(fileBrowser.headMenueIcon[2],"headMenueIcons","open","closed");
                fileBrowser.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.timerAnimation.stop();
            }
        } else if(kindOf.equals("Instruction_Manuel")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (!fileBrowser.getPackageManager().canRequestPackageInstalls()) {
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s",
                            fileBrowser.getPackageName()))), fileBrowser.ASK_PERMISSION_PKGInstall);
                }
            }
            fileBrowser.createList_systemUrl(2,4);
            fileBrowser.changeIcon(headMenueIcon01[1],"sideLeftMenueIcons","closed","open");
        } else if (kindOf.equals("PermissionDenied")) {
            fileBrowser.checkPermission();
            fileBrowser.fragmentShutdown(fileBrowser.showMessage,0);
        } else if (kindOf.equals("Extern_Device_Permission")) {
            fileBrowser.fragmentShutdown(fileBrowser.showMessage,0);
        } else if(kindOf.equals("mailSendRequest")) {

            String ms = fileBrowser.createSendEmail.mailTx.getText().toString();
            ClipboardManager clipBoard;
            clipBoard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", ms);
            clipBoard.setPrimaryClip(clip);
            fileBrowser.createSendEmail.startMailSend();

        } else if(kindOf.endsWith("Document_Save")) {

            String tx = requestedText.getText().toString().replace(" ","");

            if (tx.contains(".") && !tx.contains(","))
                tx = tx.substring(0, tx.lastIndexOf("."));

            if (kindOf.startsWith("Tx")) {
                fileBrowser.createTxEditor.buildTxFile(devicePath.substring(0,devicePath.lastIndexOf("/")), tx);
            } else if (kindOf.startsWith("PDF") || kindOf.startsWith("Pdf")) {
                fileBrowser.createTxEditor.timeImage.setVisibility(View.VISIBLE);
                fileBrowser.createTxEditor.timerAnimation.start();

                if(fileBrowser.createTxEditor.kindOfFormat.equals(".txt")) {
                    fileBrowser.createTxEditor.generatePDFfromTx(fileBrowser.createTxEditor.TxEditor.getText().toString(), devicePath.substring(0, devicePath.lastIndexOf("/")), tx);
                } else if(fileBrowser.createTxEditor.kindOfFormat.equals(".pdf")) {
                    fileBrowser.createTxEditor.generatePDFfromPdf(devicePath.substring(0, devicePath.lastIndexOf("/")), tx);
                }


            } else if(kindOf.startsWith("pdfCombined")) {
                fileBrowser.createTxEditor.timeImage.setVisibility(View.VISIBLE);
                fileBrowser.createTxEditor.timerAnimation.start();

                String destination = "";
                String[] mergedFiles = tx.split(",");
                for(int i=0;i<mergedFiles.length;i++) {

                    mergedFiles[i] = devicePath +"/"+ mergedFiles[i].trim().replace(" ","");
                    if(!mergedFiles[i].endsWith(".pdf"))
                        mergedFiles[i] = mergedFiles[i] +".pdf";
                }

                destination = mergedFiles[0];
                mergedFiles = Arrays.copyOfRange(mergedFiles,1,mergedFiles.length);

                fileBrowser.createTxEditor.Merge_PdfFiles(devicePath, destination, mergedFiles);
            }
        } else if(kindOf.equals("httpsRequest")) {
            fileBrowser.webBrowserDisplay.timeImage.setVisibility(View.VISIBLE);
            fileBrowser.webBrowserDisplay.timerAnimation.start();

            if(fileBrowser.webBrowserDisplay != null && fileBrowser.webBrowserDisplay.isVisible()) {
                fileBrowser.webBrowserDisplay.webView.loadUrl(requestedText.getText().toString());
                if(fileBrowser.softKeyBoard != null && fileBrowser.softKeyBoard.isVisible()) {
                    fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);
                }
            }
        }
    }

    private ImageView createCopyButton (int line) {
        ImageView copyButton = new ImageView(fileBrowser);
        copyButton.setLayoutParams(new RelativeLayout.LayoutParams((int) (80 * xfact), (int) (80 * xfact)));
        copyButton.setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/Kopie_closed.png"));
        copyButton.setTag(line + " Kopie_closed.png");
        copyButton.setX(displayWidth/3);

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fileBrowser.timeImage.setVisibility(View.VISIBLE);
                fileBrowser.timerAnimation.start();

                String ms = "";
                    ClipboardManager clipBoard;
                    clipBoard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                    if (messageString.length > 0)
                        for (String s : messageString)
                            ms = ms + s + "\n";
                    ClipData clip = ClipData.newPlainText("label", ms);
                    clipBoard.setPrimaryClip(clip);

                String[] mess = docu_Loader("Language/" + language + "/CopiedToClippboard.txt");
                fileBrowser.messageStarter("Instruction", mess, 5000);
            }
        });

        return copyButton;
    }

    private void createSelector () {
        ImageView selector = new ImageView(fileBrowser);
        selector.setLayoutParams(new RelativeLayout.LayoutParams((int) (60 * xfact), (int) (60 * xfact)));
        selector.setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/txEditorSelector.png"));
        selector.setX(messageLayout.getWidth() -messageLayout.getWidth()/5);
        selector.setY(messageLayout.getHeight()/5);
        selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestedText.setText(requestedText.getText().toString().replace(", ,",",").replace(",,",","));
                int diff = txLengthDiff - requestedText.getText().toString().length();
                if(diff == 0)
                    selectStartPos = selectStopPos +2;
                else
                    selectStartPos = selectStopPos -diff +2;

                if(selectStopPos < requestedText.getText().toString().length()) {
                    if (requestedText.getText().toString().substring(selectStartPos).contains(", ")) {
                        selectStopPos = requestedText.getText().toString().substring(selectStartPos).indexOf(", ") + selectStartPos;
                        requestedText.setSelection(selectStartPos, selectStopPos);
                    } else {
                        selectStopPos = requestedText.getText().toString().length();
                        requestedText.setSelection(selectStartPos, selectStopPos);
                    }
                } else {
                    requestedText.setSelection(requestedText.getText().toString().length());
                    view.setEnabled(false);
                }
                txLengthDiff = requestedText.getText().toString().length();
            }
        });

        mainLinLy.addView(selector);
    }
    private class messageTimer extends Thread {
        public messageTimer() {

        }
        public void run() {

            try {
                Thread.sleep(messageTimer);
                if(kindOf.equals("PermissionDenied"))
                    getActivity().finish();
            } catch(InterruptedException e){}

            if(kindOf.startsWith("NoList")) {
                fileBrowser.closeListlinkedIcons(new ImageView[] {fileBrowser.headMenueIcon02[3]},
                        new String[]{"sideRightMenueIcons"});
            } else if(kindOf.startsWith("MediaList")) {

                if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                    fileBrowser.fragmentShutdown(fileBrowser.showList,3);

                fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "open", "running");

                String kindOfMedia = "";
                    if(kindOf.contains("Mus"))
                        kindOfMedia = "AUDIO";
                    else if(kindOf.contains("Picture") || kindOf.contains("Bilder"))
                        kindOfMedia = "PICTURES";
                    else if(kindOf.contains("Video"))
                        kindOfMedia = "VIDEO";
                    Bundle bund = new Bundle();
                    bund.putString("KIND_OF_MEDIA", kindOfMedia);
                    bund.putString("URL", "Array");
                    fileBrowser.fragmentStart(fileBrowser.showMediaDisplay, 4,"mediaDisplay", bund, 1, 1,
                            displayWidth -2, displayHeight -2);

                fileBrowser.intendStarted = true;

            } else if(kindOf.startsWith("mailNoInternet")) {

                if(fileBrowser.createSendEmail != null) {
                    fileBrowser.createSendEmail.timeImage.setVisibility(View.INVISIBLE);
                    fileBrowser.createSendEmail.timerAnimation.stop();
                }


            } else if(kindOf.equals("Instruction")) {
                fileBrowser.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.timerAnimation.stop();
            }

            else if(kindOf.equals("Instruction_LogoAccount")) {
                fileBrowser.changeIcon(fileBrowser.createTxEditor.icons[1], "TextEditorIcons", "open", "closed");
                fileBrowser.createTxEditor.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.createTxEditor.timerAnimation.stop();
            } else if(kindOf.equals("InfoContact")) {
                fileBrowser.changeIcon(fileBrowser.headMenueIcon02[6],"sideRightMenueIcons","open","closed");

                fileBrowser.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.timerAnimation.stop();
            } else if(kindOf.equals("Instruction_MailAccount")) {
                fileBrowser.keyboardTrans = mailTx;
                if(fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible()) {
                    int fact = displayHeight/18,
                            fact01 = displayHeight/18;
                    if(yfact < 0.625) {
                        fact = displayHeight / 28;
                        fact01 = 0;
                    }
                    fileBrowser.keyboardTrans = requestedText;
                    if(fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible())
                        fileBrowser.fragmentStart(fileBrowser.softKeyBoard, 6,"softKeyBoard",null,5,(int)(2*displayHeight/3 -fact),
                                displayWidth -10, (int)(displayHeight/3) +fact01);
                }
                if(calledBack.equals("InfoView")) {
                    fileBrowser.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            fileBrowser.createSendEmail.mailTx.requestFocus();
                            if (fileBrowser.createSendEmail.mailTx.getText().toString().contains("(!") && fileBrowser.createSendEmail.mailTx.getText().toString().contains("!)"))
                                fileBrowser.createSendEmail.mailTx.setSelection(fileBrowser.createSendEmail.mailTx.getText().toString().indexOf("(!"),
                                        fileBrowser.createSendEmail.mailTx.getText().toString().indexOf("!)") + 2);
                            fileBrowser.createSendEmail.mainRel.addView(fileBrowser.createSendEmail.selector);

                        }
                    });
                }

                fileBrowser.createSendEmail.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.createSendEmail.timerAnimation.stop();
            } else if(kindOf.equals("Instruction_EditorAccount")) {
                fileBrowser.keyboardTrans = TxEditor;

                if(fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible()) {
                    int fact = displayHeight/18,
                            fact01 = displayHeight/18;
                    if(yfact < 0.625) {
                        fact = displayHeight / 28;
                        fact01 = 0;
                    }
                    fileBrowser.keyboardTrans = requestedText;
                    if(fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible())
                        fileBrowser.fragmentStart(fileBrowser.softKeyBoard, 6,"softKeyBoard",null,5,(int)(2*displayHeight/3 -fact),
                                displayWidth -10, (int)(displayHeight/3) +fact01);
                }
                fileBrowser.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (fileBrowser.createTxEditor != null && fileBrowser.createTxEditor.isVisible() && fileBrowser.createTxEditor.action.equals("info"))
                            fileBrowser.createTxEditor.createTextEditorDisplay();

                        fileBrowser.createTxEditor.TxEditor.requestFocus();
                        if (fileBrowser.createTxEditor.TxEditor.getText().toString().contains("(!") && fileBrowser.createTxEditor.TxEditor.getText().toString().contains("!)"))
                            fileBrowser.createTxEditor.TxEditor.setSelection(TxEditor.getText().toString().indexOf("(!"), fileBrowser.createTxEditor.TxEditor.getText().toString().indexOf("!)") + 2);
                    }
                });

            } else if(kindOf.startsWith("successAction")) {

                kindOf = kindOf.substring(kindOf.indexOf(" ")+1);
                if(kindOf.contains("delete")) {
                    if(kindOf.endsWith("delete")) {
                        devicePath = devicePath.substring(0,devicePath.lastIndexOf("/"));
                    }

                    if (kindOf.endsWith("toTrash")) {
                        calledBy = "trashList";
                        if(fileBrowser.read_writeFileOnInternalStorage("write", "pathCollection", "PathList.txt", devicePath +"\n").length == 0) {
                            calledBy = "toTrash";
                            fileBrowser.changeIcon(headMenueIcon01[headMenueIcon01.length - 1], "sideLeftMenueIcons", "closed", "open");
                        }

                    } else if (kindOf.endsWith("fromTrash")) {
                        String[] str = fileBrowser.read_writeFileOnInternalStorage("read", "pathCollection", "PathList.txt", "str");
                        String filestr = kindOf.substring(kindOf.indexOf(" ") +1,kindOf.lastIndexOf(" "));
                        String newstr = "";
                        if(str.length > 0)
                            for(String s : str) {
                                if (!s.equals(devicePath +filestr))
                                    newstr = newstr + s + "\n";
                            }

                        fileBrowser.read_writeFileOnInternalStorage("write", "pathCollection", "PathList.txt", newstr);

                        if(arrayList.size() == 0) {
                            calledBy = "fromTrash";
                            fileBrowser.changeIcon(headMenueIcon01[headMenueIcon01.length - 1], "sideLeftMenueIcons", "open", "closed");
                        }
                    }
                }

                if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                    fileBrowser.fragmentShutdown(fileBrowser.showList,3);

                fileBrowser.changeIcon(headMenueIcon[2],"headMenueIcons","open","closed");
                fileBrowser.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.timerAnimation.stop();

            } else if(kindOf.equals("Successful_TxDocumentSave")) {

                timerRun = false;
                fileBrowser.changeIcon(fileBrowser.createTxEditor.icons[3],"TextEditorIcons", "open", "closed");
                fileBrowser.createTxEditor.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.createTxEditor.timerAnimation.stop();




            } else if(kindOf.equals("Successful_PdfDocumentSave")) {

                fileBrowser.createTxEditor.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.createTxEditor.timerAnimation.stop();
                fileBrowser.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        devicePath = fileBrowser.createTxEditor.loadedFile;
                        fileBrowser.createTxEditor.variablenInstantion("",".pdf",new String[]{});
                        fileBrowser.reloadFileBrowserDisplay();
                    }
                });

            } else if(kindOf.equals("AddLayConstruct")) {
                fileBrowser.changeIcon(fileBrowser.createTxEditor.icons[2], "TextEditorIcons","open","closed");
            }


            if(fileBrowser.createSendEmail != null && fileBrowser.createSendEmail.isVisible()) {
                fileBrowser.createSendEmail.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.createSendEmail.timerAnimation.stop();
                fileBrowser.changeIcon(fileBrowser.createSendEmail.icons[7],"mailIcons","open","closed");
                fileBrowser.changeIcon(fileBrowser.createSendEmail.icons[6],"mailIcons","open","closed");
                fileBrowser.changeIcon(fileBrowser.createSendEmail.icons[1],"mailIcons","open","closed");
            }

            if(fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                fileBrowser.changeIcon(fileBrowser.headMenueIcon[2],"headMenueIcons","open","closed");
                fileBrowser.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.timerAnimation.stop();
            }
            if(!calledBack.equals("InfoView"))
               if(fileBrowser.softKeyBoard != null && fileBrowser.softKeyBoard.isVisible())
                   fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);


            fileBrowser.fragmentShutdown(fileBrowser.showMessage,0);

        }
    }
}