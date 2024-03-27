package com.example.kotlintest.incomingnotification

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.util.*

/*
class MyNotificationListenerService : NotificationListenerService() {

    private val domainNames = """
        .ac .academy .accountant .accountants .actor .adult .ae .ae.org .ag .agency .ai .airforce .al .alsace .am .amsterdam .apartments .archi .army .art .associates .at .auction .ba .band .bar .barcelona .bargains .bayern .be .beer .berlin .best .bet .bid .bike .bingo .bio .biz .black .blog .blue .boutique .br.com .brussels .build .builders .business .buzz .bz .bzh .cab .cafe .camera .camp .capetown .capital .cards .care .careers .casa .cash .casino .catering .cc .center .ceo .ch .chat .cheap .christmas .church .city .cl .claims .cleaning .click .clinic .clothing .cloud .club .cm .cn .cn.com .co .co.at .co.nl .co.no .co.uk .co.za .coach .codes .coffee .college .cologne .com .com.br .com.cn .com.gr .com.hr .com.mx .com.pe .com.pl .com.pt .com.ua .community .company .computer .condos .construction .consulting .contractors .cooking .cool .country .coupons .courses .credit .creditcard .cricket .cruises .cx .cymru .cz .dance .date .dating .de .de.com .deals .degree .delivery .democrat .dental .desi .design .dev .diamonds .digital .direct .directory .discount .dk .dog .domains .download .durban .earth .education .email .energy .engineer .engineering .enterprises .es .estate .eu .eu.com .events .exchange .expert .exposed .express .fail .faith .family .fans .farm .fashion .fi .film .finance .financial .fish .fishing .fit .fitness .flights .florist .fm .football .forsale .foundation .fr .frl .fun .fund .furniture .futbol .fyi .gallery .game .games .garden .gb.net .gift .gifts .gives .glass .global .gmbh .gold .golf .gr .graphics .gratis .green .gripe .group .gs .guide .guru .hamburg .haus .healthcare .help .hn .hockey .holdings .holiday .horse .hospital .host .house .how .ht .hu .im .immo .immobilien .in .industries .info .ink .institute .insure .international .investments .io .irish .is .ist .istanbul .it .jetzt .jewelry .joburg .jp .jpn.com .kaufen .kim .kitchen .kiwi .koeln .la .land .lc .lease .legal .lgbt .li .life .lighting .limited .limo .link .live .loan .loans .lol .london .love .ltd .ltda .lu .maison .management .market .marketing .mba .me .media .melbourne .memorial .men .menu .miami .mn .mobi .moda .mom .money .mortgage .movie .ms .mu .mx .nagoya .name .navy .net .network .news .ngo .ninja .nl .nrw .nu .nyc .one .online .or.at .org .org.uk .paris .partners .parts .party .pe .pet .photo .photography .photos .physio .pics .pictures .pink .pizza .pl .place .plumbing .plus .poker .porn .press .pro .productions .promo .properties .pt .pub .pw .quebec .racing .recipes .red .rehab .reise .reisen .rent .rentals .repair .report .rest .restaurant .review .reviews .rip .ro .rocks .rodeo .ru.com .ruhr .run .sa.com .saarland .sale .salon .sarl .sc .school .schule .science .scot .se .se.net .services .sex .sh .shoes .shop .show .si .singles .site .sk .ski .soccer .social .software .solar .solutions .soy .space .st .store .stream .studio .study .style .sucks .supplies .supply .support .surf .surgery .sydney .systems .tattoo .tax .taxi .team .tech .technology .tennis .theater .tienda .tips .tires .tirol .to .today .tokyo .tools .top .tours .town .toys .trade .training .tv .tw .uk .uk.com .uk.net .university .uno .us .us.com .vacations .vc .vegas .ventures .vet .viajes .video .villas .vin .vip .vision .vodka .vote .voyage .wales .watch .webcam .website .wedding .wien .wiki .win .wine .work .works .world .ws .wtf .xxx .yoga .yokohama .za.com .zone
    """.trimIndent().split(" ")

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn != null) {
            val notification = sbn.notification
            val text = notification.extras.getCharSequence(Notification.EXTRA_TEXT)
            Log.d("TAG", "onNotificationPosted:text $text ")

            if (text != null) {
                val lowerCaseText = text.toString().toLowerCase()
                val foundDomains = domainNames.filter { domain -> lowerCaseText.contains(domain) }

                if (foundDomains.isNotEmpty()) {
                    val matchedDomain = foundDomains.last()
                    val startIndex = lowerCaseText.indexOf(matchedDomain)

                    if (startIndex != -1) {
                        val relevantText = lowerCaseText.substring(0, startIndex + matchedDomain.length)
                        Log.d("TAG", "onNotificationPosted:relevantText $relevantText ")

                        sendUrlToService(relevantText)
                    }
                }
            }
        }
    }


*/
/*    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn != null) {
            val notification = sbn.notification
            val text = notification.extras.getCharSequence(Notification.EXTRA_TEXT)

            Log.d("TAG", "onNotificationPosted:text $text ")


            if (text != null) {
                val lowerCaseText = text.toString().toLowerCase()
                val foundDomains = domainNames.filter { domain -> lowerCaseText.endsWith(domain) }

                if (foundDomains.isNotEmpty()) {
                    val matchedUrl = foundDomains.last()
                    Log.d("TAG", "onNotificationPosted:matchedUrl $matchedUrl ")

                    val startIndex = lowerCaseText.indexOf(matchedUrl)
                    Log.d("TAG", "onNotificationPosted:startIndex $startIndex ")

                    if (startIndex != -1) {
                        val relevantText = text.subSequence(startIndex, text.length).toString()
                        Log.d("TAG", "onNotificationPosted:relevantText $relevantText ")
                        sendUrlToService(relevantText)
                    }
                }
            }
        }
    }*//*


    private fun sendUrlToService(url: String) {
        Log.d("TAG", "onNotificationPosted: aya url $url ")

        val intent = Intent("com.example.NOTIFICATION_URL_RECEIVED")
        intent.putExtra("url", url)
        sendBroadcast(intent)
    }
*/
/*    private fun extractLinksFromText(text: String): List<String> {
        val regex =
            Regex("(http|https)://[a-zA-Z0-9./?&=%]+|www\\.[a-zA-Z0-9./?&=%]+|(?:[a-zA-Z0-9]+\\.)+[a-zA-Z]{2,6}(?::\\d{1,5})?(?:/[\\w./?=&%#\\-]*)?")

        return regex.findAll(text)
            .map { it.value }
            .toList()
    }*//*

}
*/


class MyNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn != null) {
            val notification = sbn.notification
            val title = notification.extras.getString(Notification.EXTRA_TITLE)
            val text = notification.extras.getCharSequence(Notification.EXTRA_TEXT)

            Log.d("TAG", "onNotificationPosted:title $title  text $text ")

            val links = extractLinksFromText(text.toString())

            if (links.isNotEmpty()) {
                val intent = Intent("com.example.NOTIFICATION_LINK_RECEIVED")
                intent.putExtra("links", ArrayList(links))
                sendBroadcast(intent)
            }
        }
    }

    private fun extractLinksFromText(text: String): List<String> {
        val regex =
            Regex("(http|https)://[a-zA-Z0-9./?&=%]+|www\\.[a-zA-Z0-9./?&=%]+|(?:[a-zA-Z0-9]+\\.)+[a-zA-Z]{2,6}(?::\\d{1,5})?(?:/[\\w./?=&%#\\-]*)?")

        return regex.findAll(text)
            .map { it.value }
            .toList()
    }


}

