#!/usr/bin/env python3
"""
AI Code Review Script using Gemini API
Posts friendly, constructive code reviews on GitHub pull requests
"""

import os
import sys
import json
from pathlib import Path
import google.generativeai as genai
from github import Github, GithubException

# Configuration
GEMINI_MODEL = "gemini-2.5-flash"
MAX_FILES_TO_REVIEW = 50
MAX_FILE_SIZE = 100000  # 100KB per file
REVIEW_COMMENT_MARKER = "<!-- ai-code-review-bot -->"


def setup_gemini(api_key):
    """Initialize Gemini API"""
    genai.configure(api_key=api_key)
    return genai.GenerativeModel(GEMINI_MODEL)


def load_prompt_template():
    """Load the code review prompt from markdown file"""
    prompt_path = Path(__file__).parent.parent / "prompts" / "code-review-prompt.md"
    try:
        with open(prompt_path, 'r', encoding='utf-8') as f:
            return f.read()
    except FileNotFoundError:
        print(f"Warning: Prompt file not found at {prompt_path}")
        return "Please review this code and provide constructive feedback."


def get_changed_files(repo, pr_number):
    """Fetch changed files from the pull request"""
    pr = repo.get_pull(pr_number)
    files = pr.get_files()

    changed_files = []
    total_size = 0

    for file in files:
        # Skip deleted files and very large files
        if file.status == "removed":
            continue
        if file.changes > 1000:  # Skip files with too many changes
            print(f"Skipping large file: {file.filename} ({file.changes} changes)")
            continue

        # Fetch file content
        try:
            content = repo.get_contents(file.filename, ref=pr.head.sha)
            if content.size > MAX_FILE_SIZE:
                print(f"Skipping large file: {file.filename} ({content.size} bytes)")
                continue

            decoded_content = content.decoded_content.decode('utf-8')
            total_size += len(decoded_content)

            changed_files.append({
                'filename': file.filename,
                'status': file.status,
                'additions': file.additions,
                'deletions': file.deletions,
                'changes': file.changes,
                'patch': file.patch if hasattr(file, 'patch') else None,
                'content': decoded_content
            })

            if len(changed_files) >= MAX_FILES_TO_REVIEW:
                print(f"Reached max files limit ({MAX_FILES_TO_REVIEW})")
                break

        except Exception as e:
            print(f"Error reading file {file.filename}: {e}")
            continue

    return changed_files, pr


def build_review_request(prompt_template, changed_files, pr_info):
    """Build the review request for Gemini"""
    review_request = f"{prompt_template}\n\n---\n\n"
    review_request += f"# Pull Request Information\n\n"
    review_request += f"**Title**: {pr_info.title}\n"
    if pr_info.body:
        review_request += f"**Description**: {pr_info.body}\n"
    review_request += f"\n**Files Changed**: {len(changed_files)}\n\n"

    review_request += "# Files to Review\n\n"

    for file_info in changed_files:
        review_request += f"## File: `{file_info['filename']}`\n"
        review_request += f"**Status**: {file_info['status']} "
        review_request += f"(+{file_info['additions']} -{file_info['deletions']})\n\n"

        if file_info['patch']:
            review_request += "**Changes (diff):**\n```diff\n"
            review_request += file_info['patch']
            review_request += "\n```\n\n"

        review_request += "**Full file content:**\n```java\n"
        review_request += file_info['content']
        review_request += "\n```\n\n---\n\n"

    return review_request


def generate_review(model, review_request):
    """Call Gemini API to generate code review"""
    try:
        response = model.generate_content(
            review_request,
            generation_config={
                'temperature': 0.7,
                'top_p': 0.95,
                'top_k': 40,
                'max_output_tokens': 8192,
            }
        )
        return response.text
    except Exception as e:
        print(f"Error generating review: {e}")
        return None


def find_existing_review_comment(pr, bot_login="github-actions[bot]"):
    """Find existing AI review comment on the PR"""
    try:
        comments = pr.get_issue_comments()
        for comment in comments:
            if comment.user.login == bot_login and REVIEW_COMMENT_MARKER in comment.body:
                return comment
    except Exception as e:
        print(f"Error finding existing comment: {e}")
    return None


def post_or_update_review(repo, pr_number, review_text):
    """Post review as a comment on the PR (or update existing one)"""
    pr = repo.get_pull(pr_number)

    # Format the review comment
    formatted_review = f"{REVIEW_COMMENT_MARKER}\n"
    formatted_review += "# AI Code Review\n\n"
    formatted_review += review_text
    formatted_review += "\n\n---\n"
    formatted_review += "*This review was automatically generated by AI. "
    formatted_review += "Please use your judgment and feel free to discuss any suggestions!*"

    # Try to update existing comment first
    existing_comment = find_existing_review_comment(pr)

    try:
        if existing_comment:
            existing_comment.edit(formatted_review)
            print(f"Updated existing review comment: {existing_comment.html_url}")
        else:
            comment = pr.as_issue().create_comment(formatted_review)
            print(f"Posted new review comment: {comment.html_url}")
        return True
    except GithubException as e:
        print(f"Error posting comment: {e}")
        return False


def main():
    """Main execution function"""
    # Get environment variables
    github_token = os.environ.get('GITHUB_TOKEN')
    gemini_api_key = os.environ.get('GEMINI_API_KEY')
    repo_name = os.environ.get('GITHUB_REPOSITORY')
    pr_number = os.environ.get('PR_NUMBER')
    dry_run = os.environ.get('DRY_RUN', '').lower() in ('true', '1', 'yes')

    if not all([github_token, gemini_api_key, repo_name, pr_number]):
        print("Error: Missing required environment variables")
        print(f"GITHUB_TOKEN: {'✓' if github_token else '✗'}")
        print(f"GEMINI_API_KEY: {'✓' if gemini_api_key else '✗'}")
        print(f"GITHUB_REPOSITORY: {'✓' if repo_name else '✗'}")
        print(f"PR_NUMBER: {'✓' if pr_number else '✗'}")
        sys.exit(1)

    try:
        pr_number = int(pr_number)
    except ValueError:
        print(f"Error: Invalid PR number: {pr_number}")
        sys.exit(1)

    mode_str = "DRY RUN MODE" if dry_run else "LIVE MODE"
    print(f"{'='*60}")
    print(f"Starting AI code review for {repo_name}#{pr_number}")
    print(f"Mode: {mode_str}")
    print(f"{'='*60}\n")

    # Initialize APIs
    print("Initializing Gemini...")
    model = setup_gemini(gemini_api_key)

    print("Connecting to GitHub...")
    gh = Github(github_token)
    repo = gh.get_repo(repo_name)

    # Load prompt template
    print("Loading review prompt template...")
    prompt_template = load_prompt_template()

    # Get changed files
    print("Fetching changed files...")
    changed_files, pr = get_changed_files(repo, pr_number)

    if not changed_files:
        print("No files to review (or all files were filtered out)")
        no_files_message = (
            f"{REVIEW_COMMENT_MARKER}\n"
            "# AI Code Review\n\n"
            "I didn't find any files that needed review in this PR. "
            "This might be because the changes are to configuration files, "
            "or the files are too large to review. Great job on keeping things organized!"
        )

        if dry_run:
            print("\n" + "="*60)
            print("DRY RUN: Would have posted this comment:")
            print("="*60)
            print(no_files_message)
            print("="*60)
        else:
            pr.as_issue().create_comment(no_files_message)
        return

    print(f"Found {len(changed_files)} files to review")

    # Build review request
    print("Building review request...")
    review_request = build_review_request(prompt_template, changed_files, pr)

    # Generate review
    print("Generating AI review with Gemini...")
    review_text = generate_review(model, review_request)

    if not review_text:
        print("Failed to generate review")
        sys.exit(1)

    print("Review generated successfully")

    # Format the review comment
    formatted_review = f"{REVIEW_COMMENT_MARKER}\n"
    formatted_review += "# AI Code Review\n\n"
    formatted_review += review_text
    formatted_review += "\n\n---\n"
    formatted_review += "*This review was automatically generated by AI. "
    formatted_review += "Please use your judgment and feel free to discuss any suggestions!*"

    # Post review or print for dry run
    if dry_run:
        print("\n" + "="*60)
        print("DRY RUN: Generated review (would be posted to PR):")
        print("="*60 + "\n")
        print(formatted_review)
        print("\n" + "="*60)
        print("DRY RUN: Review complete (no changes made to GitHub)")
        print("="*60)
    else:
        print("Posting review to GitHub...")
        success = post_or_update_review(repo, pr_number, review_text)

        if success:
            print("Code review completed successfully!")
        else:
            print("Failed to post review")
            sys.exit(1)


if __name__ == "__main__":
    main()
