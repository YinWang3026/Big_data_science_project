## BDS Project - Detecting Misinformation

Semester long project.

## Folder Structure

The workspace contains four folders by default, where:

- `src`: the folder to maintain sources - App.java is there
- `lib`: the folder to maintain dependencies - Need to add in the Stanford NLP library to dependencies
- `data`: the folder to maintain input data
- `output`: the folder to maintain output results

## Dependency Management

The `JAVA DEPENDENCIES` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-pack/blob/master/release-notes/v0.9.0.md#work-with-jar-files-directly).

## Running the App

App [inputfile] [algorithm]
inputfile:  [copy] - use a small sentiment file for testing
            [nocopy] - use the original sentiment file
algorithm:  [data_exploration] - Finds top 20 hashtags and @
            [n_gram] - Finds the 1-4-gram phrases
            [n_gram_pos] - Finds the 1-4-gram phrases with Parts of Speech
            [analysis_depparse] - Dependency Parser
            [analysis_ner] - Name-entity Parser
            [pos_adjnn] - Finds the 1-4-gram phrases with Parts of Speech with Adj Noun
            