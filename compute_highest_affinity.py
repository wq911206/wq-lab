# No need to process files and manipulate strings - we will
# pass in lists (of equal length) that correspond to 
# sites views. The first list is the site visited, the second is
# the user who visited the site.
# See the test cases for more details.
def highest_affinity(site_list, user_list, time_list):
# Returned string pair should be ordered by dictionary order
# I.e., if the highest affinity pair is "foo" and "bar"
# return ("bar", "foo").
    aff=dict()
    for site, user in zip(site_list, user_list):
        #if site not in aff:
        #    aff[site]=[user]
        #else:
        #    aff[site].append(user)
        aff[site] = aff.get(site, []) + [user]


    (bigcount,bigsite1,bigsite2)=(0,None,None)

    for site1 in aff.keys():
        for site2 in aff.keys():
            if site1==site2:
                continue
            count=0
            for user1 in aff[site1]:
                for user2 in aff[site2]:
                    if user1==user2:
                        count=count+1;
            if count>=bigcount: 
                (bigcount,bigsite1,bigsite2)=(count,site1,site2)

    lst=list()
    lst.append(bigsite1)
    lst.append(bigsite2)
    lst.sort()
    return (lst[0],lst[1])