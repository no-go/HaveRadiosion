package click.dummer.schenese.dialog;

import click.dummer.schenese.R;
import click.dummer.schenese.WebRadioChannel;
import click.dummer.schenese.array.ChannelList;
import click.dummer.schenese.array.SimpleArrayAdapter;
import click.dummer.schenese.listener.CallbackListener;
import click.dummer.schenese.listener.DialogFragmentWithListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Button;

public class SettingsDialog extends DialogFragmentWithListener implements OnClickListener
{
  public enum SettingsType{Main, CustomChannel, DefaultChannel, EditChannel};
  private static SettingsType settingsType = SettingsType.Main;
  private EditText channelUrl, channelName;

  // Empty constructor required for DialogFragment
  public SettingsDialog() {}

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View view;
    if (settingsType == SettingsType.EditChannel)
    {
      getDialog().setTitle(R.string.channels);
      view = inflater.inflate(R.layout.fragment_edit, container);
      Button saveButton = (Button) view.findViewById(R.id.saveButton);
      saveButton.setOnClickListener(this);
      this.channelName = (EditText) view.findViewById(R.id.channelName);
      this.channelUrl = (EditText) view.findViewById(R.id.channelUrl);
      SimpleArrayAdapter channelAdapter = new SimpleArrayAdapter(inflater.getContext());
      
      WebRadioChannel selChannel = ChannelList.getInstance().getSelectedChannel();
      if (selChannel != null)
      {
        channelName.setText(selChannel.getName());
        channelUrl.setText(selChannel.getUrl());
      }
    }
    else
    {
      getDialog().setTitle(R.string.edit);
      view = inflater.inflate(R.layout.fragment_settings, container);
      Button addButton = (Button) view.findViewById(R.id.addChannel);
      if (settingsType==SettingsType.Main) { ((ViewManager)view).removeView(addButton); }
      Button editButton = (Button) view.findViewById(R.id.editChannel);
      if (settingsType!=SettingsType.CustomChannel) { ((ViewManager)view).removeView(editButton); }
      Button editCsButton = (Button) view.findViewById(R.id.editChannels);
      if (settingsType!=SettingsType.Main) { ((ViewManager)view).removeView(editCsButton); }
      Button rmButton = (Button) view.findViewById(R.id.rmChannel);
      if (settingsType!=SettingsType.CustomChannel) { ((ViewManager)view).removeView(rmButton); }
      addButton.setOnClickListener(this);
      editButton.setOnClickListener(this);
      editCsButton.setOnClickListener(this);
      rmButton.setOnClickListener(this);
      view.forceLayout();
    }
    return view;
  }

  @Override
  public void onClick(View v)
  {
    if (v.getId()==R.id.editChannel)
    {
      CallbackListener delegateCallback = setCallbackListener(null);
      dismiss();
      showSettings(v, getFragmentManager(), "fragment_edit", SettingsDialog.class, delegateCallback, SettingsType.EditChannel);
    }
    else if (v.getId()==R.id.rmChannel)
    {
      dismiss();
      ChannelList.getInstance().getCustomChannelList().remove(ChannelList.getInstance().getSelectedChannel());
    }
    else if (v.getId()==R.id.addChannel)
    {
      CallbackListener delegateCallback = setCallbackListener(null);
      dismiss();
      ChannelList.getInstance().setSelectedChannel(-1, false);
      showSettings(v, getFragmentManager(), "fragment_edit", SettingsDialog.class, delegateCallback, SettingsType.EditChannel);
    }
    else if (v.getId()==R.id.saveButton)
    {
      String inputName = channelName.getText().toString().trim();
      if (inputName.length() < 3) {
        inputName += "--------------";
      }
      WebRadioChannel newChannel = new WebRadioChannel(inputName, channelUrl.getText().toString().trim());
      WebRadioChannel selChannel = ChannelList.getInstance().getSelectedChannel();
      if (selChannel == null)
      {
        ChannelList.getInstance().getCustomChannelList().add(newChannel);
      }
      else
      {
        selChannel.setData(newChannel);
      }
      dismiss();
    }
  }

  public static void showSettings(View v, FragmentManager fm, String dialogKey, Class<?> c, CallbackListener callback, SettingsType settingsTypeObj)
  { // close existing dialog fragments
    if (settingsTypeObj != null) { settingsType = settingsTypeObj; }
    Fragment frag = fm.findFragmentByTag(dialogKey);
    if (frag != null)
    {
      fm.beginTransaction().remove(frag).commit();
    }
    DialogFragmentWithListener editNameDialog;
    try
    {
      editNameDialog = (DialogFragmentWithListener) c.newInstance();
      editNameDialog.show(fm, dialogKey);
      editNameDialog.setCallbackListener(callback);
      //TODO: DialogFragmentWithCallback
    }
    catch (java.lang.InstantiationException e) { }
    catch (IllegalAccessException e) {}
  }

  @Override
  public void onResume()
  {
    super.onResume();
    if (settingsType == SettingsType.EditChannel) {
      ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
      params.width = ViewGroup.LayoutParams.MATCH_PARENT;
      params.height = ViewGroup.LayoutParams.MATCH_PARENT;
      getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }
  }
}
