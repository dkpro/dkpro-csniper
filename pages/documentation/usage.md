---
layout: page-fullwidth
title: "How to use CSniper"
permalink: "/documentation/usage/"
---

<div class="row">
<div class="medium-4 medium-push-8 columns" markdown="1">
<div class="panel radius" markdown="1">
**Content**
{: #toc }
*  TOC
{:toc}
</div>
</div><!-- /.medium-4.columns -->

<div class="medium-8 medium-pull-4 columns" markdown="1">

The aim of this guide is to show most of CSniper's functions and how to make use of them.
When you visit you CSniper page, you are first greeted with a login screen. Supply your login data and proceed.

## Overview

Choose any of these actions:

- *[Analysis](#analysis)* - Show parse trees for sentences
- *[Search](#search)* - Conduct a corpus search
- *[Assessment](#assessment)* - Do a corpus search and assess the found items
- *[Evaluation and Export](#evaluation-and-export)* - See statistics about your project, export
- *[Settings](#settings)* - Add or edit annotation types
- *[Manage users](#manage-users)* - Add, edit, remove users

The last two items are only available to users with an administrator role.

![Welcome page](../../images/screenshots/welcome.png)

## Analysis

This page offers constituent parsing of user-entered sentences. Simply enter a sentence, choose a parser (CSniper comes with the OpenNLP parser preconfigured), and click "Parse" - you'll be presented with an image showing the parse tree.

![Analysis page](../../images/screenshots/analysis.png)

## Search

This page is useful if you don't want to do a full blown annotation query, but just check and run some superficial tests. You cannot annotate on this page, and results and queries will not be saved.
To start a search, you have to choose a corpus and an engine, then enter a query in the engine's syntax and click "Submit query". You are then shown a table of results.

![Search page](../../images/screenshots/search.png)

The two icons to the left of each entry provide additional insight into an item:

- The magnifier icon opens an overlay window which contains the parse tree for the specific sentence.
- The text icon opens a text box at the bottom of the table which shows the corpus context for the specific sentence.

![Search page, context box](../../images/screenshots/search_2.png)

## Assessment

// TODO

## Evaluation and Export

// TODO

## Settings

On this page you can create new annotation types or edit existing ones.

Each type has to be given a unique name, and you can also set annotation goals. A short but precise description is useful for your annotators, who can refer to this description when annotating. *Show preview* let's you see how the description will be seen on the Assessment page (you can use Creole Wiki syntax here; find out more by clicking help in the navigation bar). You can also add custom columns which will be shown in the result table. They can be used by the annotators to leave notes or additional information.
Don't forget to save!

![Settings page](../../images/screenshots/settings_2.png)

## Manage users

Here you cann add or delete users, and also set an initial password (or a new password should they have forgotten theirs).
Because the name is a unique identifier, you can only set it once.
You can also disable accounts, and assign roles. Each user has to have the ROLE_USER role, you may also assign ROLE_ADMIN, which gives this user access to the settings and users pages.

![Manage Users page](../../images/screenshots/manage_users.png)



</div><!-- /.medium-8.columns -->
</div><!-- /.row -->
