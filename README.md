## BDS Project - Detecting Misinformation

Semester long project.

## Folder Structure

The workspace contains four folders by default, where:

- `src`: the folder to maintain sources 
    - *App*.java is there
- `data`: the folder to maintain input data 
    - Fake.csv has the original data
    - Fake_eval.csv has a small list for evaluation
    - Fake_500.csv has 500 entries
    - Same applies for True.csv
    - stopwords.txt contains the list of stopwords used
- `output`: the folder to maintain output results
    - See the output from program below
    - keyword_extract_500 is my extract result for the 500/500 articles
    - select_result_500 is the structured matrix
    - select_result_eval is the matrix for the eval files
- `rapidminer_src`: the folder containing Rapidminer process

## Running the App
First build the Java Maven project using the POM file and the src files (keyword_extract.java, keyword_select.java)

Running Keyword Extract
- java -cp [name.jar] bds.keyword_extract [fake_file] [true_fale]
- fake_file: Fake_500.csv Fake_eval.csv
- true_file: True_500.csv True_eval.csv
- output: posLabels.csv, nerLabels.csv, nouns.csv, nerner.csv, adj_nn.csv, pron_vb.csv, adv_vb.csv

Running Keyword Select
- java -cp [name.jar] bds.keyword_select [fake_file] [true_fale]
- fake_file: Fake_500.csv Fake_eval.csv
- true_file: True_500.csv True_eval.csv
- output: select_result.csv

RapidMiner
- Input: select_result.csv

## GitHub Link
- In case if the original folder is needed
- https://github.com/YinWang3026/bds_project
