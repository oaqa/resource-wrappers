import sys
import os

def dirCheck():
    docPath = os.path.abspath(sys.argv[0])
    docPath = os.path.dirname(docPath) + os.path.sep + "docs"
    if not os.path.exists(docPath):
        os.mkdir(docPath)

class entrez:
    from urllib.request import urlopen
    import xml.etree.ElementTree as ET
    
    baseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/"
    esearch = baseURL + "esearch.fcgi?db=gene&term="
    esummary = baseURL + "esummary.fcgi?db=gene&id="
    summarySuffix = "[gene]+AND+alive[prop]";
    efetch = baseURL + "efetch.fcgi?db=gene&retmode=xml&id=";

    def printHelp():
        """Print the input options."""
        print("Search: s [search terms]")
        print("Summary: sum [id]")
        print("Fetch: f [id]")
        print("q to quit")
    
    def search(query):
        """
        Saves the search result document and returns a list of ID's
        from a search using the query terms.
        """
        # Construct URL
        query = query.strip()
        url = entrez.esearch + query + entrez.summarySuffix
        url = url.replace(' ', '+')
        # Send request
        urlHandle = entrez.urlopen(url)
        # Read response, save locally
        docHandle = urlHandle.read()
        entrez.writeDoc(docHandle, "search_" + query + ".xml")
        # Find and return ID's
        xml = entrez.ET.XML(docHandle)
        ids = [x.text for x in xml.findall('.//Id')]
        return ids

    def summary(ID):
        """
        Write the summary document to disk and return the name and
        description from it.
        """
        # Construc URL
        url = entrez.esummary + str(ID)
        # Send request
        urlHandle = entrez.urlopen(url)
        # Read response, write to disk
        docHandle = urlHandle.read()
        entrez.writeDoc(docHandle, "summary_" + str(ID) + ".xml")
        # Find and return name and description
        xml = entrez.ET.XML(docHandle)
        name, desc = "", ""
        itemNodes = [x for x in xml.findall('.//Item')]
        for item in itemNodes:
            if item.attrib['Name'] == "Name":
                name = item.text
            elif item.attrib['Name'] == "Description":
                desc = item.text
        return name, desc

    def fetch(ID):
        """Save full record XML object for Gene ID to disk."""
        url = entrez.efetch + str(ID)
        urlHandle = entrez.urlopen(url)
        docHandle = urlHandle.read()
        entrez.writeDoc(docHandle, "fetch_" + str(ID) + ".xml")

    def writeDoc(doc, filename):
        """Write a document to disk."""
        filePtr = open("docs/"+filename, 'w+b')
        filePtr.write(doc)
        filePtr.close()

if __name__ == "__main__":
    entrez.printHelp()
    dirCheck()
    
    query = ""
    while query != 'q':
        query = input("--> ")
        querySplit= query.split(' ')
        if querySplit[0] == 's':
            terms = " ".join(querySplit[1:])
            print(entrez.search(terms))
        elif querySplit[0] == 'f':
            entrez.fetch(querySplit[1])
        elif querySplit[0] == 'sum':
            name, desc = entrez.summary(querySplit[1])
            print(name)
            print(desc)
        elif querySplit[0] == 'q':
            continue
        else:
            print("Didn't recognize command")
