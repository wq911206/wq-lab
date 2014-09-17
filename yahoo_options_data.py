import json
import sys
import re
import urllib 
from bs4 import BeautifulSoup

def contractAsJson(filename):
    html_doc = open(filename).read()
    jsonQuoteData = dict()

    soup = BeautifulSoup(html_doc)
    currPrice=''
	
    dateUrls=[];
    optionQuotes=[];
    
    tags=soup.find_all(True)
    i=0
    num=len(tags)
    for tag in tags:
	    if tag.has_attr('class'):
	        ss=str(tag.get('class')[0])
	        if(ss=='time_rtq_ticker'):
	            currPrice=float(tag.string)
	    if tag.has_attr('href'):
	        href=str(tag.get('href'))
	        if re.search('s=\S+&m=',href):
	            href=href.replace('&','&amp;')
	            dateUrls.append("http://finance.yahoo.com"+href)
	        if re.search('/q/op\?s=\S+k=',href):
	            tmp=dict();
	            i=i+1
	            tmp['Strike']=tag.string
	            tag1=tag.find_next('td')
	            tmpstr=tag1.text
	            l=len(tmpstr)
	            tmp['Symbol']=tmpstr[:l-15]
	            tmp['Type']=tmpstr[l-9:l-8]
	            tmp['Date']=tmpstr[l-15:l-9]
	            tag1=tag1.find_next('td')
	            tmp['Last']=tag1.text
	            tag1=tag1.find_next('td')
	            tmp['Change']=tag1.text
	            tag1=tag1.find_next('td')
	            tmp['Bid']=tag1.text
	            tag1=tag1.find_next('td')
	            tmp['Ask']=tag1.text
	            tag1=tag1.find_next('td')
	            tmp['Vol']=tag1.text
	            tag1=tag1.find_next('td')
	            tmp['Open']=tag1.text
	            tmpstrs=tag1.string.split(',')
	            ss=""
	            for tmpstr1 in tmpstrs:
	                ss=ss+tmpstr1
	            tmp['AOpen']=int(ss)
	            tmp['AOpenn']=num-i	            
	            optionQuotes.append(tmp)    

    optionQuotes.sort(reverse=True)
    for dic in optionQuotes:
        del dic["AOpen"]
        del dic["AOpenn"]

    jsonQuoteData = json.dumps({"currPrice":currPrice, "dateUrls":dateUrls, "optionQuotes":optionQuotes}, sort_keys=True, indent=4, separators=(',', ': '))
    
    return jsonQuoteData