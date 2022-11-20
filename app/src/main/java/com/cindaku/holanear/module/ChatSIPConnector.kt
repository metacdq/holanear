package com.cindaku.holanear.module
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.TextureView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.XMPP_HOST
import com.cindaku.holanear.activity.CallActivity
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.model.CallType
import com.cindaku.holanear.ui.inf.OnCall
import com.cindaku.holanear.utils.AudioUtils
import org.linphone.core.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatSIPConnector @Inject constructor(
    private val context: Context,
    private val chatRepository: ChatRepository,
    private val storage: Storage
)
    : SIPConnector,CoreListener,ConferenceListener {
    private var SIPCore: Core?=null
    private var callOut: Contact?=null
    private var callIn: Contact?=null
    private var onCall: OnCall?=null
    private var notificationManager: NotificationManager?=null
    private var callManager: TelephonyManager?=null
    private var gsmCallActive=false
    private var previousCallState: Call.State?=null
    private var startCall: Int=0
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            gsmCallActive = when (state) {
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    true
                }
                TelephonyManager.CALL_STATE_RINGING -> {
                    true
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    false
                }
                else -> {
                    false
                }
            }
        }
    }
    override fun connect() {
        if(SIPCore==null){
            val factory = Factory.instance()
            factory.setDebugMode(false,"CALL")
            SIPCore = factory.createCore(null, null, context)
            val username=storage.getString("jid")
            val password=storage.getString("password")
            val domain=XMPP_HOST
            val identity = Factory.instance().createAddress("sip:$username@$domain")
            val address = Factory.instance().createAddress("sip:$domain")
            address?.transport = TransportType.Tcp
            val authInfo = Factory.instance().createAuthInfo(username, username, password, null, XMPP_HOST, XMPP_HOST, null)
            val params = SIPCore!!.createAccountParams()
            params.serverAddress = address
            params.identityAddress = identity
            params.registerEnabled = true
            params.avpfMode = AVPFMode.Enabled
            params.avpfRrInterval=1
            params.outboundProxyEnabled=true
            params.transport=TransportType.Tcp
            val account = SIPCore!!.createAccount(params)
            SIPCore!!.addAccount(account)
            SIPCore!!.defaultAccount = account
            SIPCore!!.addAuthInfo(authInfo)
            SIPCore!!.addListener(this)
            SIPCore!!.enableVideoCapture(true)
            SIPCore!!.enableVideoDisplay(true)
            SIPCore!!.enableForcedIceRelay(true)
            SIPCore!!.stunServer= XMPP_HOST
            SIPCore!!.videoActivationPolicy.automaticallyAccept = true
            SIPCore!!.videoActivationPolicy.automaticallyInitiate = true
            SIPCore!!.start()
            callManager=context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            callManager?.apply {
                listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
            }
            notificationManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }else{
            if(SIPCore!!.isNetworkReachable==false){
                SIPCore!!.start()
            }
        }
    }

    override fun checkConnectiom() {
        if(SIPCore==null){
            connect()
        }else if(SIPCore!!.isNetworkReachable==false){
            SIPCore!!.start()
        }
    }

    override fun makeACall(contact: Contact, isVideo: Boolean) {
        checkConnectiom()
        contact.jId?.let {
            callOut=contact
            val remoteSipUri = "sip:"+contact.jId
            val remoteAddress = Factory.instance().createAddress(remoteSipUri)
            remoteAddress ?: return
            remoteAddress.transport=TransportType.Tcp
            val params = SIPCore!!.createCallParams(null)
            params ?: return
            params.mediaEncryption = MediaEncryption.SRTP
            params.enableVideo(isVideo)
            SIPCore!!.inviteAddressWithParams(remoteAddress, params)
        }

    }

    override fun addToGroupCall(contact: Contact) {
        SIPCore?.let { core ->
            val remoteSipUri = "sip:"+contact.jId
            val remoteAddress = Factory.instance().createAddress(remoteSipUri)
            core.conference?.addParticipant(remoteAddress!!)
        }
    }

    override fun createGroupCall(contacts: List<Contact>) {
        SIPCore?.let { core->
            val params=core.createConferenceParams()
            core.createConferenceWithParams(params)
        }
    }

    override fun hangUp() {
        if (SIPCore!!.callsNb == 0) return
        val call = if (SIPCore!!.currentCall != null) SIPCore!!.currentCall else SIPCore!!.calls[0]
        call ?: return
        call.terminate()
    }

    override fun getCore(): Core? {
        return SIPCore
    }

    override fun accept() {
        SIPCore!!.currentCall?.let {
            if(it.remoteParams!!.videoEnabled()) {
                for (camera in SIPCore!!.videoDevicesList) {
                    if (SIPCore!!.videoDevice!=null && camera != "StaticImage: Static picture") {
                        SIPCore!!.videoDevice = camera
                        break
                    }
                }
            }
            val params = SIPCore!!.createCallParams(it)
            params?.enableVideo(it.remoteParams!!.videoEnabled())
            it.acceptWithParams(params)
        }
    }

    override fun terminate() {
        SIPCore!!.currentCall?.terminate()
    }

    override fun decline() {
        SIPCore!!.currentCall?.decline(Reason.Declined)
    }

    override fun toggleSpeaker(on: Boolean) {
        for (audioDevice in SIPCore!!.audioDevices) {
            if (on && audioDevice.type == AudioDevice.Type.Earpiece) {
                SIPCore!!.currentCall?.outputAudioDevice = audioDevice
                return
            } else if (!on && audioDevice.type == AudioDevice.Type.Speaker) {
                SIPCore!!.currentCall?.outputAudioDevice = audioDevice
                return
            }
        }

    }

    override fun toggleMic(on: Boolean) {
        SIPCore!!.enableMic(on)
    }

    override fun toggleVideoVoiceCall(on: Boolean) {
        if (SIPCore!!.callsNb == 0) return
        SIPCore!!.currentCall?.let {
            val params = SIPCore!!.createCallParams(it)
            params?.enableVideo(on)
            it.update(params)
        }
    }
    override fun toggleCamera() {
        val currentDevice = SIPCore!!.videoDevice
        for (camera in SIPCore!!.videoDevicesList) {
            if (camera != currentDevice && camera != "StaticImage: Static picture") {
                SIPCore!!.videoDevice = camera
                break
            }
        }
    }

    override fun disconnect() {
        SIPCore!!.stop()
    }

    override fun setOnCall(onCallInf: OnCall) {
        onCall=onCallInf
    }

    override fun setRemoteView(view: TextureView) {
        SIPCore!!.nativeVideoWindowId = view
    }

    override fun setLocalView(view: TextureView) {
        SIPCore!!.nativePreviewWindowId = view
    }

    override fun isConnected(): Boolean {
        if(SIPCore==null){
            return false
        }
        return SIPCore!!.isNetworkReachable
    }

    override fun onMessageReceived(core: Core, chatRoom: ChatRoom, message: ChatMessage) {
        
    }

    override fun onConfiguringStatus(core: Core, status: ConfiguringState?, message: String?) {
        status?.let {

        }
    }

    override fun onChatRoomRead(core: Core, chatRoom: ChatRoom) {
        
    }

    override fun onCallCreated(core: Core, call: Call) {
        
    }

    override fun onFirstCallStarted(core: Core) {
        
    }

    override fun onCallEncryptionChanged(
        core: Core,
        call: Call,
        mediaEncryptionEnabled: Boolean,
        authenticationToken: String?
    ) {
        
    }

    override fun onVersionUpdateCheckResultReceived(
        core: Core,
        result: VersionUpdateCheckResult,
        version: String?,
        url: String?
    ) {

    }

    override fun onMessageSent(core: Core, chatRoom: ChatRoom, message: ChatMessage) {
        
    }

    override fun onChatRoomStateChanged(core: Core, chatRoom: ChatRoom, state: ChatRoom.State?) {
        
    }

    override fun onInfoReceived(core: Core, call: Call, message: InfoMessage) {

    }

    override fun onChatRoomEphemeralMessageDeleted(core: Core, chatRoom: ChatRoom) {
        
    }

    override fun onChatRoomSubjectChanged(core: Core, chatRoom: ChatRoom) {
        
    }

    override fun onCallStateChanged(core: Core, call: Call, state: Call.State?, message: String) {
        state?.let {
            val jId=call.remoteAddress.username+"@"+call.remoteAddress.domain
            var contact = chatRepository.findContactByJid(jId)
            Log.i("CALL","from "+jId+" state "+ state.name)
            if(contact==null){
                chatRepository.createAnonymousContact(jId)
                contact = chatRepository.findContactByJid(jId)
            }
            when(state){
                Call.State.OutgoingInit -> {
                    startCall=0
                    var callRecord=chatRepository.findCallByContactId(contact_id = contact!!.id!!)
                    if(callRecord==null){
                        callRecord=com.cindaku.holanear.db.entity.Call(
                            null,
                            contact.id!!,
                            0,
                            CallType.MISS_OUT,
                            false,
                           Calendar.getInstance().time
                        )
                        chatRepository.addCall(callRecord!!)
                    }
                    val out=Intent(context,CallActivity::class.java)
                    out.putExtra("isVideo", call.params.videoEnabled())
                    out.putExtra("resume",false)
                    out.putExtra("incoming",false)
                    out.putExtra("onprogress",false)
                    out.putExtra("caller",Gson().toJson(callOut))
                    out.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(out)
                }
                Call.State.OutgoingProgress -> {
                    core.callsNb.let {
                        if (core.callsNb == 1) {
                            AudioUtils.routeAudioToBluetooth(
                                context = context.applicationContext as BaseApp,
                                call = call
                            )
                        }
                    }
                }
                Call.State.StreamsRunning -> {
                    previousCallState?.let {
                        if (core.callsNb == 1) {
                            // Only try to route bluetooth / headphone / headset when the call is in StreamsRunning for the first time
                            if (previousCallState == Call.State.Connected) {
                                if (AudioUtils.isHeadsetAudioRouteAvailable(context.applicationContext as BaseApp)) {
                                    AudioUtils.routeAudioToHeadset(context.applicationContext as BaseApp,call)
                                } else if (AudioUtils.isBluetoothAudioRouteAvailable(context.applicationContext as BaseApp)) {
                                    AudioUtils.routeAudioToBluetooth(context.applicationContext as BaseApp,call)
                                }
                            }
                        }

                        if (call.currentParams.videoEnabled()) {
                            // Do not turn speaker on when video is enabled if headset or bluetooth is used
                            if (!AudioUtils.isHeadsetAudioRouteAvailable(context.applicationContext as BaseApp) && !AudioUtils.isBluetoothAudioRouteCurrentlyUsed(
                                    context.applicationContext as BaseApp,
                                    call
                                )) {
                                AudioUtils.routeAudioToSpeaker(context.applicationContext as BaseApp,call)
                            }
                        }
                    }
                }
                Call.State.Connected -> {
                    contact?.let {
                        startCall=(Calendar.getInstance().timeInMillis/1000).toInt()
                        val callRecord=chatRepository.findCallByContactId(contact_id = it.id!!)
                        if(callRecord!=null && callIn==null){
                            callRecord.type=CallType.OUT
                            chatRepository.updateCall(callRecord)
                        }else if(callRecord!=null && callOut==null){
                            callRecord.type=CallType.IN
                            chatRepository.addCall(callRecord)
                        }
                        onCall?.onCallProgress(call.params.videoEnabled())
                        notificationManager!!.cancel("CALL",contact.id!!)
                        val notifyIntent = Intent(context, CallActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra("isVideo", call.params.videoEnabled())
                            putExtra("resume", true)
                            putExtra("incoming", false)
                            putExtra("onprogress", true)
                            putExtra("caller", Gson().toJson(contact))
                        }
                        val notifyPendingIntent = PendingIntent.getActivity(
                            context, 1002, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val importance = NotificationManager.IMPORTANCE_HIGH
                            val channel = NotificationChannel(
                                contact.id.toString(),
                                "call-" + contact.id.toString(),
                                importance
                            )
                            notificationManager?.createNotificationChannel(channel)
                        }
                        val builder = NotificationCompat.Builder(context, contact!!.id.toString())
                            .setSmallIcon(R.drawable.ic_notif)
                            .setContentTitle(contact.fullName)
                            .setContentText("Call On Progress")
                            .setOngoing(true)
                            .setContentIntent(notifyPendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                        notificationManager?.notify("CALL", contact.id!!, builder.build())
                    }
                }
                Call.State.Released ,Call.State.End -> {
                    val callRecord=chatRepository.findCallByContactId(contact_id = contact!!.id!!)
                    if(callRecord!=null){
                        if(startCall!=0) {
                            val endCall=(Calendar.getInstance().timeInMillis/1000).toInt()
                            callRecord.duration = endCall - startCall
                        }
                        callRecord.ended=true
                        chatRepository.updateCall(callRecord)
                    }
                    callIn=null
                    callOut=null
                    startCall=0
                    state.let {
                        if(it==Call.State.End){
                            onCall?.onEnded()
                            Toast.makeText(context,"Call Ended",Toast.LENGTH_SHORT).show()
                        }else{
                            onCall?.onTerminated()
                        }
                    }
                    notificationManager?.let {
                        callOut?.let {
                            notificationManager!!.cancel("CALL",it.id!!)
                        }
                        callIn?.let {
                            notificationManager!!.cancel("CALL",it.id!!)
                        }
                    }
                }
                Call.State.Updating,Call.State.EarlyUpdating -> {
                    onCall?.onRelayout(call.params.videoEnabled())
                }
                Call.State.IncomingReceived,Call.State.IncomingEarlyMedia -> {
                    contact?.let {
                        startCall=0
                        callIn=contact
                        var callRecord=chatRepository.findCallByContactId(contact_id = it.id!!)
                        if(callRecord==null){
                            callRecord=com.cindaku.holanear.db.entity.Call(
                                null,
                                 it.id!!,
                                0,
                                CallType.MISS_IN,
                                false,
                                Calendar.getInstance().time
                            )
                            chatRepository.addCall(callRecord!!)
                        }
                        val notifyIntent = Intent(context, CallActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra("isVideo", call.remoteParams!!.videoEnabled())
                            putExtra("resume", false)
                            putExtra("incoming", true)
                            putExtra("onprogress", false)
                            putExtra("caller", Gson().toJson(contact))
                        }
                        val notifyPendingIntent = PendingIntent.getActivity(
                            context, 1002, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val importance = NotificationManager.IMPORTANCE_HIGH
                            val channel = NotificationChannel(
                                contact.id.toString(),
                                "call-" + contact.id.toString(),
                                importance
                            )
                            notificationManager?.createNotificationChannel(channel)
                        }
                        val builder = NotificationCompat.Builder(context, contact.id.toString())
                            .setSmallIcon(R.drawable.ic_notif)
                            .setContentTitle(contact.fullName)
                            .setContentText("Calling...")
                            .setOngoing(true)
                            .setContentIntent(notifyPendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                        notificationManager?.notify("CALL", contact.id!!, builder.build())
                        val income=Intent(context,CallActivity::class.java)
                        income.putExtra("isVideo",call.remoteParams!!.videoEnabled())
                        income.putExtra("resume",false)
                        income.putExtra("incoming",true)
                        income.putExtra("caller",Gson().toJson(this))
                        income.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(income)
                    }
                }
                Call.State.UpdatedByRemote,Call.State.EarlyUpdatedByRemote -> {
                    SIPCore!!.currentCall?.let {
                        val params = SIPCore!!.createCallParams(it)
                        params?.enableVideo(it.remoteParams!!.videoEnabled())
                        it.acceptUpdate(it.remoteParams!!)
                    }
                    onCall?.onRelayout(call.remoteParams!!.videoEnabled())
                }
                Call.State.Error -> {
                    val callRecord=chatRepository.findCallByContactId(contact_id = contact!!.id!!)
                    if(callRecord!=null){
                        if(startCall!=0) {
                            val endCall=(Calendar.getInstance().timeInMillis/1000).toInt()
                            callRecord.duration = endCall - startCall
                        }
                        callRecord.ended=true
                        chatRepository.updateCall(callRecord)
                    }
                    callIn=null
                    callOut=null
                    startCall=0
                    onCall?.onTerminated()
                    notificationManager?.let {
                        callOut?.let {
                            notificationManager!!.cancel("CALL",it.id!!)
                        }
                        callIn?.let {
                            notificationManager!!.cancel("CALL",it.id!!)
                        }
                    }
                    Toast.makeText(context,call.errorInfo.reason.name,Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Log.i("CALL",message)
                }
            }
        }
        state?.let {
            previousCallState = it
        }
    }

    override fun onNotifyPresenceReceivedForUriOrTel(
        core: Core,
        linphoneFriend: Friend,
        uriOrTel: String,
        presenceModel: PresenceModel
    ) {
        
    }

    override fun onNewSubscriptionRequested(core: Core, linphoneFriend: Friend, url: String) {
        
    }

    override fun onEcCalibrationAudioUninit(core: Core) {
        
    }

    override fun onLastCallEnded(core: Core) {
        onCall?.onEnded()
    }

    override fun onBuddyInfoUpdated(core: Core, linphoneFriend: Friend) {
        
    }

    override fun onNotifyPresenceReceived(core: Core, linphoneFriend: Friend) {
        
    }

    override fun onAccountRegistrationStateChanged(
        core: Core,
        account: Account,
        state: RegistrationState?,
        message: String
    ) {
        
    }

    override fun onAudioDevicesListUpdated(core: Core) {
        
    }

    override fun onLogCollectionUploadProgressIndication(core: Core, offset: Int, total: Int) {
        
    }

    override fun onGlobalStateChanged(core: Core, state: GlobalState?, message: String) {
        
    }

    override fun onReferReceived(core: Core, referTo: String) {
        
    }

    override fun onNetworkReachable(core: Core, reachable: Boolean) {
        if(!reachable){
            core.start()
        }
        if(reachable){
            Log.d("CALL","=========================CONNECTED TO SIP=========================")
        }else{
            Log.d("CALL","=========================DISCONNECTED TO SIP=========================")
        }
    }

    override fun onCallLogUpdated(core: Core, callLog: CallLog) {
        
    }

    override fun onConferenceStateChanged(
        core: Core,
        conference: Conference,
        state: Conference.State?
    ) {
        state?.let {
            when(it){
                Conference.State.Instantiated->{

                }
                Conference.State.Created->{
                    conference.addListener(this)
                }
                Conference.State.CreationPending->{

                }
                Conference.State.CreationFailed->{

                }
                Conference.State.Terminated->{

                }
                Conference.State.TerminationPending->{

                }
                Conference.State.TerminationFailed->{

                }
                Conference.State.Deleted->{

                }
                else -> {

                }
            }
        }
    }

    override fun onImeeUserRegistration(core: Core, status: Boolean, userId: String, info: String) {
        
    }

    override fun onAuthenticationRequested(core: Core, authInfo: AuthInfo, method: AuthMethod) {
        
    }

    override fun onTransferStateChanged(core: Core, transfered: Call, callState: Call.State?) {
        
    }

    override fun onDtmfReceived(core: Core, call: Call, dtmf: Int) {
        
    }

    override fun onEcCalibrationResult(core: Core, status: EcCalibratorStatus?, delayMs: Int) {
        
    }

    override fun onNotifyReceived(
        core: Core,
        linphoneEvent: Event,
        notifiedEvent: String,
        body: Content
    ) {
        
    }

    override fun onEcCalibrationAudioInit(core: Core) {
        
    }

    override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {
        
    }

    override fun onIsComposingReceived(core: Core, chatRoom: ChatRoom) {
        
    }

    override fun onFriendListRemoved(core: Core, friendList: FriendList) {
        
    }

    override fun onPublishStateChanged(core: Core, linphoneEvent: Event, state: PublishState?) {
        
    }

    override fun onRegistrationStateChanged(
        core: Core,
        proxyConfig: ProxyConfig,
        state: RegistrationState?,
        message: String
    ) {
        
    }

    override fun onCallStatsUpdated(core: Core, call: Call, callStats: CallStats) {

    }

    override fun onLogCollectionUploadStateChanged(
        core: Core,
        state: Core.LogCollectionUploadState?,
        info: String
    ) {
        
    }

    override fun onSubscriptionStateChanged(
        core: Core,
        linphoneEvent: Event,
        state: SubscriptionState?
    ) {
        
    }

    override fun onMessageReceivedUnableDecrypt(
        core: Core,
        chatRoom: ChatRoom,
        message: ChatMessage
    ) {
        
    }

    override fun onQrcodeFound(core: Core, result: String?) {
        
    }

    override fun onFriendListCreated(core: Core, friendList: FriendList) {
        
    }

    override fun onSubscribeReceived(
        core: Core,
        linphoneEvent: Event,
        subscribeEvent: String,
        body: Content
    ) {
        
    }

    override fun onCallIdUpdated(core: Core, previousCallId: String, currentCallId: String) {
        
    }

    override fun onSubjectChanged(conference: Conference, subject: String) {

    }

    override fun onParticipantDeviceRemoved(
        conference: Conference,
        participantDevice: ParticipantDevice
    ) {

    }

    override fun onParticipantRemoved(conference: Conference, participant: Participant) {

    }

    override fun onStateChanged(conference: Conference, newState: Conference.State?) {

    }

    override fun onParticipantAdded(conference: Conference, participant: Participant) {

    }

    override fun onParticipantAdminStatusChanged(conference: Conference, participant: Participant) {

    }

    override fun onAudioDeviceChanged(conference: Conference, audioDevice: AudioDevice) {

    }

    override fun onParticipantDeviceAdded(
        conference: Conference,
        participantDevice: ParticipantDevice
    ) {

    }

}